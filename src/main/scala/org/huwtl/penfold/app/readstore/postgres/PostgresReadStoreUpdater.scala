package org.huwtl.penfold.app.readstore.postgres

import grizzled.slf4j.Logger
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.event.{FutureTaskCreated, TaskArchived, TaskClosed, TaskCreated, TaskPayloadUpdated, TaskRequeued, TaskRescheduled, TaskStarted, TaskTriggered, _}
import org.huwtl.penfold.domain.model.Status._
import org.huwtl.penfold.domain.model.patch.Patch
import org.huwtl.penfold.domain.model.{AggregateId, AggregateVersion, Payload, Status}
import org.huwtl.penfold.readstore.EventListener

import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{StaticQuery => Q}

class PostgresReadStoreUpdater(database: Database, objectSerializer: ObjectSerializer) extends EventListener {
  private lazy val logger = Logger(getClass)

  private val success = true

  override def handle(event: Event) = {
    val result = event match {
      case e: TaskCreated => handleCreateEvent(e, Ready)
      case e: FutureTaskCreated => handleCreateEvent(e, Waiting)
      case e: TaskTriggered => handleTaskTriggeredEvent(e)
      case e: TaskStarted => handleTaskStartedEvent(e)
      case e: TaskRequeued => handleTaskRequeuedEvent(e)
      case e: TaskRescheduled => handleTaskRescheduledEvent(e)
      case e: TaskClosed => handleTaskClosedEvent(e)
      case e: TaskUnassigned => handleUnassignedEvent(e)
      case e: TaskPayloadUpdated => handleUpdatePayloadEvent(e)
      case e: TaskArchived => handleArchiveEvent(e)
      case _ =>
    }

    logger.info(s"event $event handled with result $result")

    success
  }

  private def handleCreateEvent(event: TaskCreatedEvent, status: Status) = {
    val queue = event.queueBinding.id

    val task = TaskData(event.aggregateId, event.aggregateVersion, event.created.getMillis, queue, status, event.created.getMillis, previousStatus = None, 0, event.triggerDate.getMillis, assignee = None,
      event.score, resolveSortOrder(event, status, event.score).get, event.payload, rescheduleReason = None, closeReason = None)

    val taskJson = objectSerializer.serialize(task)

    val alreadyExists = sql"""SELECT id FROM tasks WHERE id = ${task.id.value}""".as[String].firstOption
    if (!alreadyExists.isDefined) sqlu"""INSERT INTO tasks (id, data) VALUES (${task.id.value}, $taskJson::json)""".execute
  }

  private def handleTaskTriggeredEvent(event: TaskTriggered) = {
    handleTaskUpdate(event) {
      task => task.copy(previousStatus = Some(updatePreviousStatus(task)), status = Ready, statusLastModified = event.created.getMillis, sort = task.score)
    }
  }

  private def handleTaskStartedEvent(event: TaskStarted) = {
    handleTaskUpdate(event) {
      task => task.copy(
        previousStatus = Some(updatePreviousStatus(task)),
        status = Started,
        statusLastModified = event.created.getMillis,
        attempts = task.attempts + 1,
        sort = event.created.getMillis,
        assignee = event.assignee,
        payload = patchPayloadIfExists(task, event.payloadUpdate))
    }
  }

  private def handleTaskRequeuedEvent(event: TaskRequeued) = {
    handleTaskUpdate(event) {
      task => {
        val score = event.score.getOrElse(task.score)
        task.copy(
          previousStatus = Some(updatePreviousStatus(task)),
          status = Ready,
          statusLastModified = event.created.getMillis,
          score = score,
          sort = score,
          assignee = event.assignee,
          payload = patchPayloadIfExists(task, event.payloadUpdate))
      }
    }
  }

  private def handleTaskRescheduledEvent(event: TaskRescheduled) = {
    handleTaskUpdate(event) {
      task => {
        task.copy(
          previousStatus = Some(updatePreviousStatus(task)),
          status = Waiting,
          statusLastModified = event.created.getMillis,
          score = event.score.getOrElse(task.score),
          sort = event.triggerDate.getMillis,
          triggerDate = event.triggerDate.getMillis,
          rescheduleReason = event.reason,
          assignee = event.assignee,
          payload = patchPayloadIfExists(task, event.payloadUpdate))
      }
    }
  }

  private def handleTaskClosedEvent(event: TaskClosed) = {
    handleTaskUpdate(event) {
      task => {
        task.copy(
          previousStatus = Some(updatePreviousStatus(task)),
          status = Closed,
          statusLastModified = event.created.getMillis,
          sort = event.created.getMillis,
          closeReason = event.reason,
          assignee = None,
          payload = patchPayloadIfExists(task, event.payloadUpdate))
      }
    }
  }

  private def handleUnassignedEvent(event: TaskUnassigned) = {
    handleTaskUpdate(event) {
      task => {
        task.copy(
          assignee = None,
          payload = patchPayloadIfExists(task, event.payloadUpdate))
      }
    }
  }

  private def handleUpdatePayloadEvent(event: TaskPayloadUpdated) = {
    handleTaskUpdate(event) {
      task => {
        task.copy(
          sort = resolveSortOrder(event, task.status, task.score).getOrElse(task.sort),
          score = event.score.getOrElse(task.score),
          payload = patchPayloadIfExists(task, Some(event.payloadUpdate)))
      }
    }
  }

  private def handleArchiveEvent(event: TaskArchived) = {
    sqlu"""INSERT INTO archived (id, data) (SELECT t.id, t.data FROM tasks t WHERE t.id = ${event.aggregateId.value})""".execute
    sqlu"""DELETE FROM tasks WHERE id = ${event.aggregateId.value} AND (data->>'version')::bigint = ${event.aggregateVersion.previous.number}""".execute
  }

  private def handleTaskUpdate(event: Event)(updatedFields: TaskData => TaskData) = {
    existing(event.aggregateId, event.aggregateVersion) match {
      case Some(task) =>
        val defaultsApplied = task.copy(version = event.aggregateVersion, rescheduleReason = None, closeReason = None)
        val updatedTaskJson = objectSerializer.serialize(updatedFields(defaultsApplied))
        sqlu"""UPDATE tasks SET data = $updatedTaskJson::json WHERE id = ${event.aggregateId.value} AND (data->>'version')::bigint = ${event.aggregateVersion.previous.number}""".execute
      case None =>
    }
  }

  private def updatePreviousStatus(existingTask: TaskData) = {
    PreviousStatus(existingTask.status, existingTask.statusLastModified)
  }

  private def patchPayloadIfExists(existingTask: TaskData, payloadUpdate: Option[Patch]): Payload = {
    payloadUpdate.map(update => Payload(update.exec(existingTask.payload.content))).getOrElse(existingTask.payload)
  }

  private def resolveSortOrder(event: Event, status: Status, existingScore: Long) = {
    event match {
      case e: TaskCreated => Some(e.score)
      case e: FutureTaskCreated => Some(e.triggerDate.getMillis)
      case e: TaskPayloadUpdated if status == Ready => Some(e.score getOrElse existingScore)
      case e: TaskPayloadUpdated => None
      case _ => Some(event.created.getMillis)
    }
  }

  private def existing(id: AggregateId, version: AggregateVersion) = {
    val json = sql"""SELECT data FROM tasks WHERE id = ${id.value} AND (data->>'version')::bigint = ${version.previous.number}""".as[String].firstOption
    json.map(objectSerializer.deserialize[TaskData])
  }
}
