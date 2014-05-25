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
import org.huwtl.penfold.domain.model.Status.{Waiting, Started, Ready}
import org.huwtl.penfold.domain.event._
import org.huwtl.penfold.readstore.EventRecord
import org.huwtl.penfold.domain.model.patch.Replace
import org.huwtl.penfold.domain.event.TaskPayloadUpdated
import org.huwtl.penfold.domain.event.FutureTaskCreated
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.readstore.TaskRecord
import org.huwtl.penfold.domain.event.TaskCreated
import org.huwtl.penfold.domain.event.TaskStarted
import org.huwtl.penfold.domain.model.patch.Value
import org.huwtl.penfold.readstore.PreviousStatus
import org.huwtl.penfold.domain.model.patch.Patch
import org.huwtl.penfold.domain.model.QueueBinding
import org.huwtl.penfold.support.TestModel

class MongoReadStoreUpdaterTest extends Specification with EmbedConnection {
  sequential

  trait context extends Scope {
    val aggregateId = AggregateId(UUID.randomUUID().toString)
    val queueId = TestModel.queueId
    val payload = Payload(Map("field1" -> "123", "inner" -> Map("field2" -> 1)))
    val binding = QueueBinding(queueId)
    val assignee = TestModel.assignee
    val created = TestModel.createdDate
    val triggerDate =  TestModel.triggerDate
    val score = triggerDate.getMillis
    val lastVersion = AggregateVersion(2)
    val serializer = new EventSerializer
    val taskCreatedEvent = TaskCreated(aggregateId, AggregateVersion(1), created, binding, triggerDate, payload, score)
    val taskStartedEvent = TaskStarted(aggregateId, AggregateVersion(2), created, Some(assignee))
    val taskRequeuedEvent = TaskRequeued(aggregateId, AggregateVersion(3), created)

    val mongoClient = MongoClient("localhost", embedConnectionPort())
    val database = mongoClient("penfoldtest")
    val readStore = new MongoReadStore(database, Indexes(Nil), new ObjectSerializer, new DateTimeSource)
    val readStoreUpdater = new MongoReadStoreUpdater(database, new MongoEventTracker("tracking", database), new ObjectSerializer)
  }

  "create task and start" in new context {
    readStoreUpdater.handle(EventRecord(EventSequenceId(1), taskCreatedEvent))
    readStoreUpdater.handle(EventRecord(EventSequenceId(2), taskStartedEvent))

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TaskRecord(aggregateId, lastVersion, created, binding, Started, taskStartedEvent.created, Some(PreviousStatus(Ready, created)), Some(assignee), triggerDate, score, created.getMillis, payload)))
  }

  "update payload of ready task" in new context {
    val updateTime = new DateTime(2014, 2, 22, 13, 0, 0, 0)
    val updatedPayload = Payload(Map("field1" -> "123", "inner" -> 1))
    val payloadUpdate = Patch(List(Replace("/inner", Value(1))))
    val updatedScore = updateTime.getMillis
    val taskPayloadUpdatedEvent = TaskPayloadUpdated(aggregateId, AggregateVersion(2), updateTime, payloadUpdate, None, Some(updatedScore))

    readStoreUpdater.handle(EventRecord(EventSequenceId(1), taskCreatedEvent))
    readStoreUpdater.handle(EventRecord(EventSequenceId(2), taskPayloadUpdatedEvent))

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TaskRecord(aggregateId, lastVersion, created, binding, Ready, created, None, None, triggerDate, updatedScore, updatedScore, updatedPayload)))
  }

  "update payload of waiting task" in new context {
    val updateTime = new DateTime(2014, 2, 22, 13, 0, 0, 0)
    val updatedPayload = Payload(Map("field1" -> "123", "inner" -> 1))
    val payloadUpdate = Patch(List(Replace("/inner", Value(BigInt(1)))))
    val updatedScore = new DateTime(2014, 2, 22, 14, 0, 0, 0).getMillis
    val futureTaskCreatedEvent = FutureTaskCreated(aggregateId, AggregateVersion(1), created, QueueBinding(queueId), triggerDate, payload, score)
    val taskPayloadUpdatedEvent = TaskPayloadUpdated(aggregateId, AggregateVersion(2), updateTime, payloadUpdate, None, Some(updatedScore))

    readStoreUpdater.handle(EventRecord(EventSequenceId(1), futureTaskCreatedEvent))
    readStoreUpdater.handle(EventRecord(EventSequenceId(2), taskPayloadUpdatedEvent))

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TaskRecord(aggregateId, lastVersion, created, binding, Waiting, created, None, None, triggerDate, updatedScore, updateTime.getMillis, updatedPayload)))
  }

  "ignore duplicate events" in new context {
    readStoreUpdater.handle(EventRecord(EventSequenceId(1), taskCreatedEvent))
    readStoreUpdater.handle(EventRecord(EventSequenceId(2), taskStartedEvent))
    readStoreUpdater.handle(EventRecord(EventSequenceId(2), taskCreatedEvent))

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TaskRecord(aggregateId, lastVersion, created, binding, Started, taskStartedEvent.created, Some(PreviousStatus(Ready, created)), Some(assignee), triggerDate, score, created.getMillis, payload)))
  }

  "remove task on archive" in new context {
    readStoreUpdater.handle(EventRecord(EventSequenceId(1), taskCreatedEvent))
    readStoreUpdater.handle(EventRecord(EventSequenceId(2), TaskArchived(aggregateId, taskCreatedEvent.aggregateVersion.next, created)))

    val task = readStore.retrieveBy(aggregateId)
    task must beNone
  }

  "requeue started task and clear assignee if present" in new context {
    readStoreUpdater.handle(EventRecord(EventSequenceId(1), taskCreatedEvent))
    readStoreUpdater.handle(EventRecord(EventSequenceId(2), taskStartedEvent))
    readStoreUpdater.handle(EventRecord(EventSequenceId(2), taskRequeuedEvent))

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TaskRecord(aggregateId, AggregateVersion(3), created, binding, Ready, taskRequeuedEvent.created, Some(PreviousStatus(Started, created)), None, triggerDate, score, score, payload)))
  }

  "ignore events on aggregate version mismatch" in new context {
    readStoreUpdater.handle(EventRecord(EventSequenceId(1), taskCreatedEvent))
    readStoreUpdater.handle(EventRecord(EventSequenceId(2), taskStartedEvent))

    val unexpectedVersion = taskStartedEvent.aggregateVersion
    readStoreUpdater.handle(EventRecord(EventSequenceId(3), TaskArchived(taskStartedEvent.aggregateId, taskStartedEvent.aggregateVersion, created)))

    val task = readStore.retrieveBy(aggregateId)
    task must beSome(TaskRecord(aggregateId, lastVersion, created, binding, Started, taskStartedEvent.created, Some(PreviousStatus(Ready, created)), Some(assignee), triggerDate, score, created.getMillis, payload))
  }
}
