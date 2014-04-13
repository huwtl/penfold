package org.huwtl.penfold.app.readstore.mongodb

import org.huwtl.penfold.readstore.{EventTracker, EventListener}
import com.mongodb.casbah.Imports._
import org.huwtl.penfold.domain.model.Status._
import org.huwtl.penfold.domain.model.{Status, QueueId}
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.event._
import org.huwtl.penfold.domain.event.JobCompleted
import org.huwtl.penfold.readstore.EventRecord
import org.huwtl.penfold.domain.event.JobCancelled
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.domain.event.JobTriggered
import scala.Some
import org.huwtl.penfold.domain.event.JobStarted
import com.mongodb.DuplicateKeyException
import grizzled.slf4j.Logger
import scala.util.Try

class MongoReadStoreUpdater(database: MongoDB, tracker: EventTracker, objectSerializer: ObjectSerializer) extends EventListener {
  private lazy val logger = Logger(getClass)

  private val success = true

  lazy private val jobsCollection = database("jobs")

  override def handle(eventRecord: EventRecord) = {
    eventRecord.event match {
      case e: JobCreated => handleCreateEvent(e, Ready)
      case e: FutureJobCreated => handleCreateEvent(e, Waiting)
      case e: JobTriggered => handleUpdateStatusEvent(e, Ready, e.queues)
      case e: JobStarted => handleUpdateStatusEvent(e, Started, List(e.queue))
      case e: JobCompleted => handleUpdateStatusEvent(e, Completed, List(e.queue))
      case e: JobCancelled => handleUpdateStatusEvent(e, Cancelled, e.queues)
      case _ =>
    }

    tracker.trackEvent(eventRecord.id)

    success
  }

  private def handleCreateEvent(event: JobCreatedEvent, status: Status) = {
    val score = event.triggerDate.getMillis

    val queue = event.binding.queues.head

    val job = MongoDBObject(
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

    Try(jobsCollection.insert(job)) recover {
      case e: DuplicateKeyException => logger.info("job creation event already handled, ignoring", e)
      case e => throw e
    }
  }

  private def handleUpdateStatusEvent(event: Event, status: Status, queues: List[QueueId]) = {
    val query = MongoDBObject("_id" -> event.aggregateId.value, "version" -> event.aggregateVersion.previous.number)

    jobsCollection.findOne(query) match {
      case Some(job) => {
        val update = $set(
          "version" -> event.aggregateVersion.number,
          "status" -> status.name,
          "sort" -> resolveSortOrder(event, job.as[Long]("score"))
        )
        jobsCollection.update(query, update)
      }
      case None =>
    }
  }

  private def resolveSortOrder(event: Event, score: Long) = {
    event match {
      case e: JobCreated => score
      case e: JobTriggered => score
      case e: FutureJobCreated => e.triggerDate.getMillis
      case _ => event.created.getMillis
    }
  }
}
