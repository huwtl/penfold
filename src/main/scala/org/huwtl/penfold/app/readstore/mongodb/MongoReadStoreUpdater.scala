package org.huwtl.penfold.app.readstore.mongodb

import org.huwtl.penfold.readstore.{EventTracker, EventListener}
import com.mongodb.casbah.Imports._
import org.huwtl.penfold.domain.model.Status._
import org.huwtl.penfold.domain.model.{Payload, Status}
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.event._
import org.huwtl.penfold.domain.event.TaskClosed
import org.huwtl.penfold.readstore.EventRecord
import org.huwtl.penfold.domain.event.TaskCreated
import org.huwtl.penfold.domain.event.TaskTriggered
import scala.Some
import org.huwtl.penfold.domain.event.TaskStarted
import com.mongodb.DuplicateKeyException
import grizzled.slf4j.Logger
import com.mongodb.util.JSON
import org.huwtl.penfold.app.support.RegisterBigIntConversionHelpers
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import org.joda.time.DateTime

class MongoReadStoreUpdater(database: MongoDB, tracker: EventTracker, objectSerializer: ObjectSerializer) extends EventListener {
  private lazy val logger = Logger(getClass)

  RegisterBigIntConversionHelpers()
  RegisterJodaTimeConversionHelpers()

  private val success = true

  lazy private val tasksCollection = database("tasks")

  override def handle(eventRecord: EventRecord) = {
    eventRecord.event match {
      case e: TaskCreated => handleCreateEvent(e, Ready)
      case e: FutureTaskCreated => handleCreateEvent(e, Waiting)
      case e: TaskTriggered => handleUpdateStatusEvent(e, Ready)
      case e: TaskStarted => handleUpdateStatusEvent(e, Started)
      case e: TaskRequeued => handleUpdateStatusEvent(e, Ready)
      case e: TaskClosed => handleUpdateStatusEvent(e, Closed)
      case e: TaskPayloadUpdated => handleUpdatePayloadEvent(e)
      case e: TaskArchived => handleArchiveEvent(e)
      case _ =>
    }

    tracker.trackEvent(eventRecord.id)

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

  private def handleUpdateStatusEvent(event: Event, status: Status) = {
    val query = updateByIdVersion(event)

    tasksCollection.findOne(query) match {
      case Some(task) => {
        val update = $set(
          "version" -> event.aggregateVersion.number,
          "previousStatus" -> Map("status" -> task.as[String]("status"), "statusLastModified" -> task.as[DateTime]("statusLastModified")),
          "status" -> status.name,
          "statusLastModified" -> event.created.toDate,
          "assignee" -> resolveAssignee(event, task.getAs[String]("assignee")),
          "concluder" -> resolveConclusionField(event, _.concluder.map(_.username)),
          "conclusionType" -> resolveConclusionField(event, _.conclusionType),
          "sort" -> resolveSortOrder(event, status, task.as[Long]("score")).get
        )
        tasksCollection.update(query, update)
      }
      case None =>
    }
  }

  private def handleUpdatePayloadEvent(event: TaskPayloadUpdated) = {
    val query = updateByIdVersion(event)

    tasksCollection.findOne(query) match {
      case Some(task) => {
        val status = Status.from(task.as[String]("status")).get
        val existingScore = task.as[Long]("score")
        val existingSort = task.as[Long]("sort")
        val payload = objectSerializer.deserialize[Payload](JSON.serialize(task.as[String]("payload")))

        val update = $set(
          "version" -> event.aggregateVersion.number,
          "payload" -> event.payloadUpdate.exec(payload.content),
          "score" -> event.score.getOrElse(existingScore),
          "sort" -> resolveSortOrder(event, status, existingScore).getOrElse(existingSort)
        )
        tasksCollection.update(query, update)
      }
      case None =>
    }
  }

  private def handleArchiveEvent(event: TaskArchived) = {
    val query = updateByIdVersion(event)

    tasksCollection.remove(query)
  }

  private def resolveAssignee(event: Event, previousAssignee: Option[String]) = {
    event match {
      case e: TaskRequeued => None
      case e: TaskStarted => e.assignee.map(_.username)
      case _ => previousAssignee
    }
  }

  private def resolveConclusionField(event: Event, fieldValue: TaskClosed => Any) = {
    event match {
      case e: TaskClosed => fieldValue(e)
      case _ => None
    }
  }

  private def resolveSortOrder(event: Event, status: Status, existingScore: Long) = {
    event match {
      case e: TaskCreated => Some(e.score)
      case e: FutureTaskCreated => Some(e.triggerDate.getMillis)
      case e: TaskTriggered => Some(existingScore)
      case e: TaskRequeued => Some(existingScore)
      case e: TaskPayloadUpdated if status == Ready => Some(e.score getOrElse existingScore)
      case e: TaskPayloadUpdated => None
      case _ => Some(event.created.getMillis)
    }
  }

  private def updateByIdVersion(event: Event) = MongoDBObject("_id" -> event.aggregateId.value, "version" -> event.aggregateVersion.previous.number)
}
