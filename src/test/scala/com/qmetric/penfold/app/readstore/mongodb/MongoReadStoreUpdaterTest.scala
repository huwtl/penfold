package com.qmetric.penfold.app.readstore.mongodb

import com.github.athieriot.EmbedConnection
import com.qmetric.penfold.domain.model._
import com.qmetric.penfold.readstore.EventSequenceId
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.joda.time.DateTime
import com.qmetric.penfold.app.support.json.{ObjectSerializer, EventSerializer}
import com.mongodb.casbah.Imports._
import com.qmetric.penfold.app.support.DateTimeSource
import java.util.UUID
import com.qmetric.penfold.domain.event._
import com.qmetric.penfold.readstore.EventRecord
import com.qmetric.penfold.domain.model.patch.Replace
import com.qmetric.penfold.domain.event.TaskPayloadUpdated
import com.qmetric.penfold.domain.model.AggregateId
import com.qmetric.penfold.domain.model.patch.Value
import com.qmetric.penfold.domain.model.patch.Patch
import com.qmetric.penfold.support.TestModel
import com.qmetric.penfold.domain.model.Status.{Ready, Started, Closed}

class MongoReadStoreUpdaterTest extends Specification with EmbedConnection {
  sequential

  trait context extends Scope {
    val aggregateId = AggregateId(UUID.randomUUID().toString)
    val payload = Payload(Map("field1" -> "123", "inner" -> Map("field2" -> 1)))
    val lastVersion = AggregateVersion(2)
    val serializer = new EventSerializer
    val taskCreatedEvent = TestModel.events.createdEvent.copy(aggregateId = aggregateId, payload = payload)
    val futureTaskCreatedEvent = TestModel.events.futureCreatedEvent.copy(aggregateId = aggregateId, payload = payload)
    val taskStartedEvent = TestModel.events.startedEvent.copy(aggregateId = aggregateId)
    val taskTriggeredEvent = TestModel.events.triggeredEvent.copy(aggregateId = aggregateId)
    val taskUnassignedEvent = TestModel.events.unassignedEvent.copy(aggregateId = aggregateId, aggregateVersion = AggregateVersion(5))
    val taskReassignedEvent = TestModel.events.reassignedEvent.copy(aggregateId = aggregateId, aggregateVersion = AggregateVersion(3), assignee = User("user2"))
    val taskClosedEvent = TestModel.events.closedEvent.copy(aggregateId = aggregateId)
    val taskRequeuedEvent = TestModel.events.requeuedEvent.copy(aggregateId = aggregateId)
    val taskRescheduledEvent = TestModel.events.rescheduledEvent.copy(aggregateId = aggregateId)
    val archivedEvent = TestModel.events.archivedEvent.copy(aggregateId = aggregateId)

    val mongoClient = MongoClient("localhost", embedConnectionPort())
    val database = mongoClient("penfoldtest")
    val taskMapper = new MongoTaskMapper(new ObjectSerializer)
    val readStore = new MongoReadStore(database, Indexes(Nil), taskMapper, new PaginatedQueryService(database, taskMapper), new DateTimeSource)
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

    task must beEqualTo(Some(TestModel.readModels.startedTask.copy(id = aggregateId, version = lastVersion, payload = payload)))
  }

  "trigger future task" in new context {
    handleEvents(futureTaskCreatedEvent, taskTriggeredEvent)

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TestModel.readModels.triggeredTask.copy(id = aggregateId, version = lastVersion, payload = payload)))
  }

  "unassign task" in new context {
    handleEvents(taskCreatedEvent, taskStartedEvent, taskClosedEvent, taskRequeuedEvent, taskUnassignedEvent)

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TestModel.readModels.readyTask.copy(
      id = aggregateId,
      version = taskUnassignedEvent.aggregateVersion,
      payload = payload,
      previousStatus = Some(TestModel.previousStatus.copy(status = Closed)),
      assignee = None)))
  }

  "reassign task" in new context {
    handleEvents(taskCreatedEvent, taskStartedEvent, taskReassignedEvent)

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TestModel.readModels.startedTask.copy(
      id = aggregateId,
      version = taskReassignedEvent.aggregateVersion,
      payload = payload,
      assignee = Some(taskReassignedEvent.assignee))))
  }

  "close task" in new context {
    handleEvents(taskCreatedEvent, taskStartedEvent, taskClosedEvent)

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TestModel.readModels.closedTask.copy(id = aggregateId, version = AggregateVersion(3), payload = payload)))
  }

  "reschedule future task" in new context {
    handleEvents(taskCreatedEvent, taskStartedEvent, taskRescheduledEvent)

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TestModel.readModels.rescheduledTask.copy(id = aggregateId, version = AggregateVersion(3), payload = payload)))
  }

  "update payload of ready task" in new context {
    val updateTime = new DateTime(2014, 2, 22, 13, 0, 0, 0)
    val updatedPayload = Payload(Map("field1" -> "123", "inner" -> 1))
    val payloadUpdate = Patch(List(Replace("/inner", Value(1))))
    val updatedScore = updateTime.getMillis
    val taskPayloadUpdatedEvent = TaskPayloadUpdated(aggregateId, AggregateVersion(2), updateTime, payloadUpdate, None, Some(updatedScore))
    handleEvents(taskCreatedEvent, taskPayloadUpdatedEvent)

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TestModel.readModels.readyTask.copy(id = aggregateId, version = lastVersion, score = updatedScore, sort = updatedScore, payload = updatedPayload)))
  }

  "update payload of non-ready task without changing sort order" in new context {
    val updateTime = new DateTime(2014, 2, 22, 13, 0, 0, 0)
    val updatedPayload = Payload(Map("field1" -> "123", "inner" -> 1))
    val payloadUpdate = Patch(List(Replace("/inner", Value(BigInt(1)))))
    val updatedScore = new DateTime(2014, 2, 22, 14, 0, 0, 0).getMillis
    val taskPayloadUpdatedEvent = TaskPayloadUpdated(aggregateId, AggregateVersion(2), updateTime, payloadUpdate, None, Some(updatedScore))
    handleEvents(futureTaskCreatedEvent, taskPayloadUpdatedEvent)

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TestModel.readModels.waitingTask.copy(id = aggregateId, version = lastVersion, score = updatedScore, payload = updatedPayload)))
  }

  "ignore duplicate events" in new context {
    handleEvents(taskCreatedEvent, taskStartedEvent, taskCreatedEvent)

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TestModel.readModels.startedTask.copy(id = aggregateId, version = lastVersion, payload = payload)))
  }

  "remove task on archive" in new context {
    handleEvents(taskCreatedEvent, archivedEvent)

    val task = readStore.retrieveBy(aggregateId)
    task must beNone
  }

  "requeue task" in new context {
    handleEvents(taskCreatedEvent, taskStartedEvent, taskClosedEvent, taskRequeuedEvent)

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TestModel.readModels.readyTask.copy(
      id = aggregateId,
      version = taskRequeuedEvent.aggregateVersion,
      payload = payload,
      previousStatus = Some(TestModel.previousStatus.copy(status = Closed)),
      assignee = Some(TestModel.assignee))))
  }

  "ignore events on aggregate version mismatch" in new context {
    val unexpectedVersion = taskStartedEvent.aggregateVersion
    handleEvents(taskCreatedEvent, taskStartedEvent, archivedEvent.copy(aggregateVersion = unexpectedVersion))

    val task = readStore.retrieveBy(aggregateId)
    task must beSome(TestModel.readModels.startedTask.copy(id = aggregateId, version = lastVersion, payload = payload))
  }
}
