package org.huwtl.penfold.app.readstore.mongodb

import org.huwtl.penfold.readstore.{EventTracker, EventListener}
import com.mongodb.casbah.Imports._
import org.huwtl.penfold.domain.model.Status._
import org.huwtl.penfold.domain.model.{Status, QueueId}
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.event._
import org.huwtl.penfold.domain.event.TaskCompleted
import org.huwtl.penfold.readstore.EventRecord
import org.huwtl.penfold.domain.event.TaskCancelled
import org.huwtl.penfold.domain.event.TaskCreated
import org.huwtl.penfold.domain.event.TaskTriggered
import scala.Some
import org.huwtl.penfold.domain.event.TaskStarted
import com.mongodb.DuplicateKeyException
import grizzled.slf4j.Logger
import scala.util.Try

class MongoReadStoreUpdater(database: MongoDB, tracker: EventTracker, objectSerializer: ObjectSerializer) extends EventListener {
  private lazy val logger = Logger(getClass)

  private val success = true

  lazy private val tasksCollection = database("tasks")

  override def handle(eventRecord: EventRecord) = {
    eventRecord.event match {
      case e: TaskCreated => handleCreateEvent(e, Ready)
      case e: FutureTaskCreated => handleCreateEvent(e, Waiting)
      case e: TaskTriggered => handleUpdateStatusEvent(e, Ready, e.queues)
      case e: TaskStarted => handleUpdateStatusEvent(e, Started, List(e.queue))
      case e: TaskCompleted => handleUpdateStatusEvent(e, Completed, List(e.queue))
      case e: TaskCancelled => handleUpdateStatusEvent(e, Cancelled, e.queues)
      case _ =>
    }

    tracker.trackEvent(eventRecord.id)

    success
  }

  private def handleCreateEvent(event: TaskCreatedEvent, status: Status) = {
    val score = event.triggerDate.getMillis

    val queue = event.binding.queues.head

    val task = MongoDBObject(
      "_id" -> event.aggregateId.value,
      "version" -> event.aggregateVersion.number,
      "created" -> event.created.toDate,
      "queue" -> queue.id.value,
      "status" -> status.name,
      "triggerDate" -> event.triggerDate.toDate,
      "payload" -> event.payload.content,
      "sort" -> resolveSortOrder(event, score),
      "score" -> score
    )

    Try(tasksCollection.insert(task)) recover {
      case e: DuplicateKeyException => logger.info("task creation event already handled, ignoring", e)
      case e => throw e
    }
  }

  private def handleUpdateStatusEvent(event: Event, status: Status, queues: List[QueueId]) = {
    val query = MongoDBObject("_id" -> event.aggregateId.value, "version" -> event.aggregateVersion.previous.number)

    tasksCollection.findOne(query) match {
      case Some(task) => {
        val update = $set(
          "version" -> event.aggregateVersion.number,
          "status" -> status.name,
          "sort" -> resolveSortOrder(event, task.as[Long]("score"))
        )
        tasksCollection.update(query, update)
      }
      case None =>
    }
  }

  private def resolveSortOrder(event: Event, score: Long) = {
    event match {
      case e: TaskCreated => score
      case e: TaskTriggered => score
      case e: FutureTaskCreated => e.triggerDate.getMillis
      case _ => event.created.getMillis
    }
  }
}
