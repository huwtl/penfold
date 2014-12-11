package org.huwtl.penfold.app.readstore.mongodb

import org.huwtl.penfold.readstore.{EventTracker, EventListener, EventRecord}
import com.mongodb.casbah.Imports._
import org.huwtl.penfold.domain.model.Status._
import org.huwtl.penfold.domain.model.{Payload, Status}
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.event._
import com.mongodb.DuplicateKeyException
import grizzled.slf4j.Logger
import com.mongodb.util.JSON
import org.huwtl.penfold.app.support.RegisterBigIntConversionHelpers
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import org.joda.time.DateTime
import scala.Predef._
import org.huwtl.penfold.domain.event.TaskRequeued
import org.huwtl.penfold.domain.event.TaskRescheduled
import org.huwtl.penfold.domain.event.TaskPayloadUpdated
import org.huwtl.penfold.domain.event.FutureTaskCreated
import org.huwtl.penfold.domain.event.TaskArchived
import org.huwtl.penfold.domain.event.TaskTriggered
import scala.Some
import org.huwtl.penfold.domain.event.TaskCreated
import org.huwtl.penfold.domain.event.TaskStarted
import org.huwtl.penfold.domain.event.TaskClosed
import org.joda.time.format.DateTimeFormat
import org.huwtl.penfold.domain.model.patch.Patch

class MongoReadStoreUpdater(database: MongoDB, tracker: EventTracker, objectSerializer: ObjectSerializer) extends EventListener {
  private lazy val logger = Logger(getClass)

  private val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

  RegisterBigIntConversionHelpers()
  RegisterJodaTimeConversionHelpers()

  private val success = true

  lazy private val tasksCollection = database("tasks")

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

    val task = MongoDBObject(
      "_id" -> event.aggregateId.value,
      "version" -> event.aggregateVersion.number,
      "created" -> event.created,
      "queue" -> queue.value,
      "status" -> status.name,
      "statusLastModified" -> event.created,
      "triggerDate" -> event.triggerDate,
      "searchableTriggerDay" -> triggerDaySearchField(event.triggerDate),
      "payload" -> event.payload.content,
      "sort" -> resolveSortOrder(event, status, event.score).get,
      "score" -> event.score
    )

    try {
      tasksCollection.insert(task)
    }
    catch {
      case e: DuplicateKeyException => logger.info("task creation event already handled, ignoring", e)
      case e: Exception => throw e
    }
  }

  private def handleTaskTriggeredEvent(event: TaskTriggered) = {
    handleTaskUpdate(event) {task =>
      val existingScore = task.as[Long]("score")
      Map(
        "previousStatus" -> updatePreviousStatus(task),
        "status" -> Ready.name,
        "statusLastModified" -> event.created.toDate,
        "sort" -> existingScore
      )
    }
  }

  private def handleTaskStartedEvent(event: TaskStarted) = {
    handleTaskUpdate(event) {task =>
      Map(
        "previousStatus" -> updatePreviousStatus(task),
        "status" -> Started.name,
        "statusLastModified" -> event.created.toDate,
        "sort" -> event.created.getMillis,
        "assignee" -> event.assignee.map(_.username),
        "payload" -> patchPayloadIfExists(task, event.payloadUpdate)
      )
    }
  }

  private def handleTaskRequeuedEvent(event: TaskRequeued) = {
    handleTaskUpdate(event) {task =>
      val score = event.score.getOrElse(task.as[Long]("score"))
      Map(
        "previousStatus" -> updatePreviousStatus(task),
        "status" -> Ready.name,
        "statusLastModified" -> event.created.toDate,
        "sort" -> score,
        "assignee" -> event.assignee.map(_.username),
        "payload" -> patchPayloadIfExists(task, event.payloadUpdate),
        "score" -> score
      )
    }
  }

  private def handleTaskRescheduledEvent(event: TaskRescheduled) = {
    handleTaskUpdate(event) {task =>
      val score = event.score.getOrElse(task.as[Long]("score"))
      Map(
        "previousStatus" -> updatePreviousStatus(task),
        "status" -> Waiting.name,
        "statusLastModified" -> event.created.toDate,
        "sort" -> event.triggerDate.getMillis,
        "triggerDate" -> event.triggerDate,
        "searchableTriggerDay" -> triggerDaySearchField(event.triggerDate),
        "rescheduleType" -> event.rescheduleType,
        "assignee" -> event.assignee.map(_.username),
        "payload" -> patchPayloadIfExists(task, event.payloadUpdate),
        "score" -> score
      )
    }
  }

  private def handleTaskClosedEvent(event: TaskClosed) = {
    handleTaskUpdate(event) {task =>
      Map(
        "previousStatus" -> updatePreviousStatus(task),
        "status" -> Closed.name,
        "statusLastModified" -> event.created.toDate,
        "sort" -> event.created.getMillis,
        "conclusionType" -> event.conclusionType,
        "assignee" -> event.assignee.map(_.username),
        "payload" -> patchPayloadIfExists(task, event.payloadUpdate)
      )
    }
  }

  private def handleUnassignedEvent(event: TaskUnassigned) = {
    handleTaskUpdate(event) {task =>
      Map(
        "assignee" -> None,
        "payload" -> patchPayloadIfExists(task, event.payloadUpdate)
      )
    }
  }

  private def handleUpdatePayloadEvent(event: TaskPayloadUpdated) = {
    handleTaskUpdate(event) {task =>
      val status = Status.from(task.as[String]("status")).get
      val existingScore = task.as[Long]("score")
      val existingSort = task.as[Long]("sort")
      Map(
        "payload" -> patchPayloadIfExists(task, Some(event.payloadUpdate)),
        "score" -> event.score.getOrElse(existingScore),
        "sort" -> resolveSortOrder(event, status, existingScore).getOrElse(existingSort)
      )
    }
  }

  private def handleArchiveEvent(event: TaskArchived) = {
    val query = updateByIdVersion(event)

    tasksCollection.remove(query)
  }

  private def handleTaskUpdate(event: Event)(updatedFields: MongoDBObject => Map[String, Any]) = {
    val query = updateByIdVersion(event)

    tasksCollection.findOne(query) match {
      case Some(task) => {
        val defaults = Map("version" -> event.aggregateVersion.number, "rescheduleType" -> None, "conclusionType" -> None)
        val updated = updatedFields(task)
        val update = $set((defaults ++ updated).toSeq: _*)
        tasksCollection.update(query, update)
      }
      case None =>
    }
  }

  private def updatePreviousStatus(existingTaskRecord: MongoDBObject) = {
    Map("status" -> existingTaskRecord.as[String]("status"), "statusLastModified" -> existingTaskRecord.as[DateTime]("statusLastModified"))
  }

  private def patchPayloadIfExists(existingTaskRecord: MongoDBObject, payloadUpdate: Option[Patch]) = {
    val payload = objectSerializer.deserialize[Payload](JSON.serialize(existingTaskRecord.as[String]("payload")))
    payloadUpdate.map(_.exec(payload.content)).getOrElse(payload.content)
  }

  private def triggerDaySearchField(triggerDate: DateTime) = dateFormatter.print(triggerDate)


  private def resolveSortOrder(event: Event, status: Status, existingScore: Long) = {
    event match {
      case e: TaskCreated => Some(e.score)
      case e: FutureTaskCreated => Some(e.triggerDate.getMillis)
      case e: TaskPayloadUpdated if status == Ready => Some(e.score getOrElse existingScore)
      case e: TaskPayloadUpdated => None
      case _ => Some(event.created.getMillis)
    }
  }

  private def updateByIdVersion(event: Event) = MongoDBObject("_id" -> event.aggregateId.value, "version" -> event.aggregateVersion.previous.number)
}
