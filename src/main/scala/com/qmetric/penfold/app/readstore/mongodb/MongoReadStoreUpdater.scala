package com.qmetric.penfold.app.readstore.mongodb

import com.qmetric.penfold.readstore.{EventTracker, EventListener}
import com.mongodb.casbah.Imports._
import com.qmetric.penfold.domain.model.Status._
import com.qmetric.penfold.domain.model.{Payload, Status}
import com.qmetric.penfold.app.support.json.ObjectSerializer
import com.qmetric.penfold.domain.event._
import com.mongodb.DuplicateKeyException
import grizzled.slf4j.Logger
import com.mongodb.util.JSON
import com.qmetric.penfold.app.support.RegisterBigIntConversionHelpers
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import org.joda.time.DateTime
import scala.Predef._
import com.qmetric.penfold.domain.event.TaskRequeued
import com.qmetric.penfold.readstore.EventRecord
import com.qmetric.penfold.domain.event.TaskRescheduled
import com.qmetric.penfold.domain.event.TaskPayloadUpdated
import com.qmetric.penfold.domain.event.FutureTaskCreated
import com.qmetric.penfold.domain.event.TaskArchived
import com.qmetric.penfold.domain.event.TaskTriggered
import scala.Some
import com.qmetric.penfold.domain.event.TaskCreated
import com.qmetric.penfold.domain.event.TaskStarted
import com.qmetric.penfold.domain.event.TaskClosed
import scala.reflect.{ClassTag, classTag}

class MongoReadStoreUpdater(database: MongoDB, tracker: EventTracker, objectSerializer: ObjectSerializer) extends EventListener {
  private lazy val logger = Logger(getClass)

  RegisterBigIntConversionHelpers()
  RegisterJodaTimeConversionHelpers()

  private val success = true

  lazy private val tasksCollection = database("tasks")

  override def handle(eventRecord: EventRecord) = {
    val result = eventRecord.event match {
      case e: TaskCreated => handleCreateEvent(e, Ready)
      case e: FutureTaskCreated => handleCreateEvent(e, Waiting)
      case e: TaskTriggered => handleUpdateStatusEvent(e, Ready)
      case e: TaskStarted => handleUpdateStatusEvent(e, Started)
      case e: TaskRequeued => handleUpdateStatusEvent(e, Ready)
      case e: TaskRescheduled => handleUpdateStatusEvent(e, Waiting)
      case e: TaskClosed => handleUpdateStatusEvent(e, Closed)
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
          "triggerDate" -> resolveFieldValue[TaskRescheduled, Option[DateTime]](event, task.getAs[DateTime]("triggerDate"), e => Some(e.triggerDate)),
          "previousStatus" -> Map("status" -> task.as[String]("status"), "statusLastModified" -> task.as[DateTime]("statusLastModified")),
          "status" -> status.name,
          "statusLastModified" -> event.created.toDate,
          "assignee" -> resolveAssignee(event, task.getAs[String]("assignee")),
          "rescheduleType" -> resolveFieldValue[TaskRescheduled, Option[String]](event, task.getAs[String]("rescheduleType"), _.rescheduleType),
          "concluder" -> resolveFieldValue[TaskClosed, Option[String]](event, None, _.concluder.map(_.username)),
          "conclusionType" -> resolveFieldValue[TaskClosed, Option[String]](event, None, _.conclusionType),
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
      case e: TaskStarted => e.assignee.map(_.username).orElse(previousAssignee)
      case e: TaskRescheduled => e.assignee.map(_.username).orElse(previousAssignee)
      case _ => previousAssignee
    }
  }

  private def resolveFieldValue[E <: Event : ClassTag, F](event: Event, default: F, fieldValue: E => F) = {
    event match {
      case e if classTag[E].runtimeClass.isInstance(e) => fieldValue(e.asInstanceOf[E])
      case _ => default
    }
  }

  private def resolveSortOrder(event: Event, status: Status, existingScore: Long) = {
    event match {
      case e: TaskCreated => Some(e.score)
      case e: FutureTaskCreated => Some(e.triggerDate.getMillis)
      case e: TaskTriggered => Some(existingScore)
      case e: TaskRequeued => Some(existingScore)
      case e: TaskRescheduled => Some(e.triggerDate.getMillis)
      case e: TaskPayloadUpdated if status == Ready => Some(e.score getOrElse existingScore)
      case e: TaskPayloadUpdated => None
      case _ => Some(event.created.getMillis)
    }
  }

  private def updateByIdVersion(event: Event) = MongoDBObject("_id" -> event.aggregateId.value, "version" -> event.aggregateVersion.previous.number)
}
