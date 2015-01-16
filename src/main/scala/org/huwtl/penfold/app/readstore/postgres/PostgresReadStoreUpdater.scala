package org.huwtl.penfold.app.readstore.postgres

import org.huwtl.penfold.readstore.{EventTracker, EventListener, EventRecord}
import org.huwtl.penfold.domain.model.Status._
import org.huwtl.penfold.domain.model.{AggregateVersion, AggregateId, Payload, Status}
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.event._
import grizzled.slf4j.Logger
import org.joda.time.DateTime
import scala.Predef._
import org.huwtl.penfold.domain.event.TaskRequeued
import org.huwtl.penfold.domain.event.TaskRescheduled
import org.huwtl.penfold.domain.event.TaskPayloadUpdated
import org.huwtl.penfold.domain.event.FutureTaskCreated
import org.huwtl.penfold.domain.event.TaskArchived
import org.huwtl.penfold.domain.event.TaskTriggered
import org.huwtl.penfold.domain.event.TaskCreated
import org.huwtl.penfold.domain.event.TaskStarted
import org.huwtl.penfold.domain.event.TaskClosed
import org.joda.time.format.DateTimeFormat
import org.huwtl.penfold.domain.model.patch.Patch

import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{StaticQuery => Q}
import java.sql.SQLException

class PostgresReadStoreUpdater(database: Database, tracker: EventTracker, objectSerializer: ObjectSerializer) extends EventListener {
  private lazy val logger = Logger(getClass)

  private val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

  private val dupSqlState = "23505"

  private val success = true

  override def handle(eventRecord: EventRecord) = {
    val result = eventRecord.event match {
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

    logger.info(s"event ${eventRecord.id} handled with result $result")

    tracker.trackEvent(eventRecord.id)

    logger.info(s"event ${eventRecord.id} tracked")

    success
  }

  private def handleCreateEvent(event: TaskCreatedEvent, status: Status) = {
    val queue = event.queueBinding.id

    val task = TaskData(event.aggregateId, event.aggregateVersion, event.created, queue, status, event.created, previousStatus = None, event.triggerDate, assignee = None,
      event.score, resolveSortOrder(event, status, event.score).get, event.payload, rescheduleType = None, conclusionType = None)

    val taskJson = objectSerializer.serialize(task)

    database.withDynSession {
      try {
        sqlu"""INSERT INTO tasks (id, data) VALUES (${task.id.value}, $taskJson)""".execute
      }
      catch {
        case e: SQLException if e.getSQLState == dupSqlState => {
          logger.info("task creation event already handled, ignoring", e)
        }
      }
    }
  }

  private def handleTaskTriggeredEvent(event: TaskTriggered) = {
    handleTaskUpdate(event) {
      task => task.copy(previousStatus = Some(updatePreviousStatus(task)), status = Ready, statusLastModified = event.created, sort = task.score)
    }
  }

  private def handleTaskStartedEvent(event: TaskStarted) = {
    handleTaskUpdate(event) {
      task => task.copy(
        previousStatus = Some(updatePreviousStatus(task)),
        status = Started,
        statusLastModified = event.created,
        sort = event.created.getMillis,
        assignee = event.assignee,
        payload = patchPayloadIfExists(task, event.payloadUpdate))
    }
  }

  private def handleTaskRequeuedEvent(event: TaskRequeued) = {
    handleTaskUpdate(event) {
      task =>
        val score = event.score.getOrElse(task.as[Long]("score"))
        Map("previousStatus" -> updatePreviousStatus(task), "status" -> Ready.name, "statusLastModified" -> event.created.toDate, "sort" -> score,
          "assignee" -> event.assignee.map(_.username), "payload" -> patchPayloadIfExists(task, event.payloadUpdate), "score" -> score)
    }
  }

  private def handleTaskRescheduledEvent(event: TaskRescheduled) = {
    handleTaskUpdate(event) {
      task =>
        val score = event.score.getOrElse(task.as[Long]("score"))
        Map("previousStatus" -> updatePreviousStatus(task), "status" -> Waiting.name, "statusLastModified" -> event.created.toDate, "sort" -> event.triggerDate.getMillis,
          "triggerDate" -> event.triggerDate, "rescheduleType" -> event.rescheduleType,
          "assignee" -> event.assignee.map(_.username), "payload" -> patchPayloadIfExists(task, event.payloadUpdate), "score" -> score)
    }
  }

  private def handleTaskClosedEvent(event: TaskClosed) = {
    handleTaskUpdate(event) {
      task =>
        Map("previousStatus" -> updatePreviousStatus(task), "status" -> Closed.name, "statusLastModified" -> event.created.toDate, "sort" -> event.created.getMillis,
          "conclusionType" -> event.conclusionType, "assignee" -> event.assignee.map(_.username), "payload" -> patchPayloadIfExists(task, event.payloadUpdate))
    }
  }

  private def handleUnassignedEvent(event: TaskUnassigned) = {
    handleTaskUpdate(event) {
      task =>
        Map("assignee" -> None, "payload" -> patchPayloadIfExists(task, event.payloadUpdate))
    }
  }

  private def handleUpdatePayloadEvent(event: TaskPayloadUpdated) = {
    handleTaskUpdate(event) {
      task =>
        val status = Status.from(task.as[String]("status")).get
        val existingScore = task.as[Long]("score")
        val existingSort = task.as[Long]("sort")
        Map("payload" -> patchPayloadIfExists(task, Some(event.payloadUpdate)), "score" -> event.score.getOrElse(existingScore),
          "sort" -> resolveSortOrder(event, status, existingScore).getOrElse(existingSort))
    }
  }

  private def handleArchiveEvent(event: TaskArchived) = {
    database.withDynSession {
      sqlu"""DELETE FROM tasks WHERE id = ${event.aggregateId.value} AND data->'version' = ${event.aggregateVersion.previous.number})""".execute
    }
  }

  private def handleTaskUpdate(event: Event)(updatedFields: TaskData => TaskData) = {
    existing(event.aggregateId, event.aggregateVersion) match {
      case Some(task) =>
        val defaultsApplied = task.copy(version = event.aggregateVersion, rescheduleType = None, conclusionType = None)
        val updatedTaskJson = objectSerializer.serialize(updatedFields(defaultsApplied))
        database.withDynSession {
          sqlu"""UPDATE tasks SET data = $updatedTaskJson WHERE id = ${event.aggregateId.value} AND data->'version' = ${event.aggregateVersion.previous.number})""".execute
        }
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
    database.withDynSession {
      val json = sql"""SELECT data FROM tasks WHERE id = ${id.value} AND data->'version' = ${version.previous.number}""".as[String].firstOption
      json.map(objectSerializer.deserialize[TaskData])
    }
  }
}