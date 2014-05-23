package org.huwtl.penfold.app.readstore.mongodb

import com.github.athieriot.EmbedConnection
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.readstore.EventSequenceId
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.joda.time.DateTime
import org.huwtl.penfold.app.support.json.{ObjectSerializer, EventSerializer}
import com.mongodb.casbah.Imports._
import org.huwtl.penfold.app.support.DateTimeSource
import java.util.UUID
import org.huwtl.penfold.domain.event._
import org.huwtl.penfold.readstore.EventRecord
import org.huwtl.penfold.domain.model.patch.Replace
import org.huwtl.penfold.domain.event.TaskPayloadUpdated
import org.huwtl.penfold.domain.event.FutureTaskCreated
import org.huwtl.penfold.domain.model.AggregateId
import scala.Some
import org.huwtl.penfold.domain.event.TaskCreated
import org.huwtl.penfold.domain.event.TaskStarted
import org.huwtl.penfold.domain.model.patch.Value
import org.huwtl.penfold.domain.model.patch.Patch
import org.huwtl.penfold.domain.model.QueueBinding
import org.huwtl.penfold.support.TestModel
import org.huwtl.penfold.domain.model.Status.Completed

class MongoReadStoreUpdaterTest extends Specification with EmbedConnection {
  sequential

  trait context extends Scope {
    val aggregateId = AggregateId(UUID.randomUUID().toString)
    val payload = Payload(Map("field1" -> "123", "inner" -> Map("field2" -> 1)))
    val lastVersion = AggregateVersion(2)
    val serializer = new EventSerializer
    val taskCreatedEvent = TaskCreated(aggregateId, AggregateVersion(1), TestModel.createdDate, QueueBinding(TestModel.queueId), TestModel.triggerDate, payload, TestModel.score)
    val futureTaskCreatedEvent = FutureTaskCreated(aggregateId, AggregateVersion(1), TestModel.createdDate, QueueBinding(TestModel.queueId), TestModel.triggerDate, payload, TestModel.score)
    val taskStartedEvent = TaskStarted(aggregateId, AggregateVersion(2), TestModel.createdDate, Some(TestModel.assignee))
    val taskRequeuedEvent = TaskRequeued(aggregateId, AggregateVersion(4), TestModel.createdDate)
    val taskCompletedEvent = TaskCompleted(aggregateId, AggregateVersion(3), TestModel.createdDate, Some(TestModel.concluder), Some(TestModel.conclusionType))
    val archivedEvent = TaskArchived(aggregateId, taskCreatedEvent.aggregateVersion.next, TestModel.createdDate)

    val mongoClient = MongoClient("localhost", embedConnectionPort())
    val database = mongoClient("penfoldtest")
    val readStore = new MongoReadStore(database, Indexes(Nil), new ObjectSerializer, new DateTimeSource)
    val readStoreUpdater = new MongoReadStoreUpdater(database, new MongoEventTracker("tracking", database), new ObjectSerializer)

    def handleEvents(events: Event*) = {
      events.zipWithIndex.foreach{
        case (event, index) => readStoreUpdater.handle(EventRecord(EventSequenceId(index + 1), event))
      }
    }
  }

  "create task and start" in new context {
    handleEvents(taskCreatedEvent, taskStartedEvent)

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TestModel.startedTask.copy(id = aggregateId, version = lastVersion, payload = payload)))
  }

  "complete task" in new context {
    handleEvents(taskCreatedEvent, taskStartedEvent, taskCompletedEvent)

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TestModel.completedTask.copy(id = aggregateId, version = AggregateVersion(3), payload = payload)))
  }

  "update payload of ready task" in new context {
    val updateTime = new DateTime(2014, 2, 22, 13, 0, 0, 0)
    val updatedPayload = Payload(Map("field1" -> "123", "inner" -> 1))
    val payloadUpdate = Patch(List(Replace("/inner", Value(1))))
    val updatedScore = updateTime.getMillis
    val taskPayloadUpdatedEvent = TaskPayloadUpdated(aggregateId, AggregateVersion(2), updateTime, payloadUpdate, None, Some(updatedScore))
    handleEvents(taskCreatedEvent, taskPayloadUpdatedEvent)

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TestModel.readyTask.copy(id = aggregateId, version = lastVersion, score = updatedScore, sort = updatedScore, payload = updatedPayload)))
  }

  "update payload of waiting task" in new context {
    val updateTime = new DateTime(2014, 2, 22, 13, 0, 0, 0)
    val updatedPayload = Payload(Map("field1" -> "123", "inner" -> 1))
    val payloadUpdate = Patch(List(Replace("/inner", Value(BigInt(1)))))
    val updatedScore = new DateTime(2014, 2, 22, 14, 0, 0, 0).getMillis
    val taskPayloadUpdatedEvent = TaskPayloadUpdated(aggregateId, AggregateVersion(2), updateTime, payloadUpdate, None, Some(updatedScore))
    handleEvents(futureTaskCreatedEvent, taskPayloadUpdatedEvent)

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TestModel.waitingTask.copy(id = aggregateId, version = lastVersion, score = updatedScore, sort = updateTime.getMillis, payload = updatedPayload)))
  }

  "ignore duplicate events" in new context {
    handleEvents(taskCreatedEvent, taskStartedEvent, taskCreatedEvent)

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TestModel.startedTask.copy(id = aggregateId, version = lastVersion, payload = payload)))
  }

  "remove task on archive" in new context {
    handleEvents(taskCreatedEvent, archivedEvent)

    val task = readStore.retrieveBy(aggregateId)
    task must beNone
  }

  "requeue completed task and clear assignee and conclusion fields if present" in new context {
    handleEvents(taskCreatedEvent, taskStartedEvent, taskCompletedEvent, taskRequeuedEvent)

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TestModel.task.copy(id = aggregateId, version = taskRequeuedEvent.aggregateVersion, payload = payload, previousStatus = Some(TestModel.previousStatus.copy(status = Completed)))))
  }

  "ignore events on aggregate version mismatch" in new context {
    val unexpectedVersion = taskStartedEvent.aggregateVersion
    handleEvents(taskCreatedEvent, taskStartedEvent, archivedEvent.copy(aggregateVersion = unexpectedVersion))

    val task = readStore.retrieveBy(aggregateId)
    task must beSome(TestModel.startedTask.copy(id = aggregateId, version = lastVersion, payload = payload))
  }
}
