package com.qmetric.penfold.app.readstore.postgres

import java.util.UUID

import com.qmetric.penfold.app.support.DateTimeSource
import com.qmetric.penfold.app.support.json.{EventSerializer, ObjectSerializer}
import com.qmetric.penfold.domain.event.{Event, TaskPayloadUpdated}
import com.qmetric.penfold.domain.model.Status.Closed
import com.qmetric.penfold.domain.model.patch.{Patch, Replace, Value}
import com.qmetric.penfold.domain.model.{AggregateId, AggregateVersion, Payload}
import com.qmetric.penfold.support.{PostgresSpecification, TestModel}
import org.joda.time.DateTime
import org.specs2.specification.Scope

import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{StaticQuery => Q}

class PostgresReadStoreUpdaterTest extends PostgresSpecification {
  sequential

  val database = newDatabase()

  trait context extends Scope {
    val aggregateId = AggregateId(UUID.randomUUID().toString)
    val payload = Payload(Map("field1" -> "123", "inner" -> Map("field2" -> 1)))
    val lastVersion = AggregateVersion(2)
    val serializer = new EventSerializer
    val objectSerializer = new ObjectSerializer
    val taskCreatedEvent = TestModel.events.createdEvent.copy(aggregateId = aggregateId, payload = payload)
    val futureTaskCreatedEvent = TestModel.events.futureCreatedEvent.copy(aggregateId = aggregateId, payload = payload)
    val taskStartedEvent = TestModel.events.startedEvent.copy(aggregateId = aggregateId)
    val taskTriggeredEvent = TestModel.events.triggeredEvent.copy(aggregateId = aggregateId)
    val taskUnassignedEvent = TestModel.events.unassignedEvent.copy(aggregateId = aggregateId, aggregateVersion = AggregateVersion(5))
    val taskClosedEvent = TestModel.events.closedEvent.copy(aggregateId = aggregateId)
    val taskCancelledEvent = TestModel.events.cancelEvent.copy(aggregateId = aggregateId)
    val taskRequeuedEvent = TestModel.events.requeuedEvent.copy(aggregateId = aggregateId)
    val taskRescheduledEvent = TestModel.events.rescheduledEvent.copy(aggregateId = aggregateId)
    val archivedEvent = TestModel.events.archivedEvent.copy(aggregateId = aggregateId)
    val readStore = new PostgresReadStore(database, new PaginatedQueryService(database, objectSerializer, Aliases.empty), objectSerializer, new DateTimeSource, Aliases.empty)
    val readStoreUpdater = new PostgresReadStoreUpdater(database, objectSerializer)

    def handleEvents(events: Event*) = {
      database.withDynTransaction {
        events.zipWithIndex.foreach {
          case (event, index) => readStoreUpdater.handle(event)
        }
      }
    }
  }

  "create task and start" in new context {
    handleEvents(taskCreatedEvent, taskStartedEvent)

    database.withDynTransaction {
      val task = readStore.retrieveBy(aggregateId)

      task must beEqualTo(Some(TestModel.readModels.startedTask.copy(id = aggregateId, version = lastVersion, payload = payload)))
    }
  }

  "trigger future task" in new context {
    handleEvents(futureTaskCreatedEvent, taskTriggeredEvent)

    database.withDynTransaction {
      val task = readStore.retrieveBy(aggregateId)

      task must beEqualTo(Some(TestModel.readModels.triggeredTask.copy(id = aggregateId, version = lastVersion, payload = payload)))
    }
  }

  "unassign task" in new context {
    handleEvents(taskCreatedEvent, taskStartedEvent, taskClosedEvent, taskRequeuedEvent, taskUnassignedEvent)

    database.withDynTransaction {
      val task = readStore.retrieveBy(aggregateId)

      task must beEqualTo(Some(TestModel.readModels.readyTask.copy(
        id = aggregateId,
        version = taskUnassignedEvent.aggregateVersion,
        payload = payload,
        attempts = 1,
        previousStatus = Some(TestModel.previousStatus.copy(status = Closed)),
        assignee = None)))
    }
  }

  "close task" in new context {
    handleEvents(taskCreatedEvent, taskStartedEvent, taskClosedEvent)

    database.withDynTransaction {
      val task = readStore.retrieveBy(aggregateId)

      task must beEqualTo(Some(TestModel.readModels.closedTask.copy(id = aggregateId, version = AggregateVersion(3), payload = payload)))
    }
  }

  "cancel task" in new context {
    handleEvents(taskCreatedEvent, taskStartedEvent, taskCancelledEvent)

    database.withDynTransaction {
      val task = readStore.retrieveBy(aggregateId)

      task must beEqualTo(Some(TestModel.readModels.cancelledTask.copy(id = aggregateId, version = AggregateVersion(3), payload = payload)))
    }
  }

  "reschedule future task" in new context {
    handleEvents(taskCreatedEvent, taskStartedEvent, taskRescheduledEvent)

    database.withDynTransaction {
      val task = readStore.retrieveBy(aggregateId)

      task must beEqualTo(Some(TestModel.readModels.rescheduledTask.copy(id = aggregateId, version = AggregateVersion(3), attempts = 1, payload = payload)))
    }
  }

  "update payload of ready task" in new context {
    val updateTime = new DateTime(2014, 2, 22, 13, 0, 0, 0)
    val updatedPayload = Payload(Map("field1" -> "123", "inner" -> 1))
    val payloadUpdate = Patch(List(Replace("/inner", Value(1))))
    val updatedScore = updateTime.getMillis
    val taskPayloadUpdatedEvent = TaskPayloadUpdated(aggregateId, AggregateVersion(2), updateTime, payloadUpdate, None, Some(updatedScore))
    handleEvents(taskCreatedEvent, taskPayloadUpdatedEvent)

    database.withDynTransaction {
      val task = readStore.retrieveBy(aggregateId)

      task must beEqualTo(Some(TestModel.readModels.readyTask.copy(id = aggregateId, version = lastVersion, score = updatedScore, sort = updatedScore, payload = updatedPayload)))
    }
  }

  "update payload of non-ready task without changing sort order" in new context {
    val updateTime = new DateTime(2014, 2, 22, 13, 0, 0, 0)
    val updatedPayload = Payload(Map("field1" -> "123", "inner" -> 1))
    val payloadUpdate = Patch(List(Replace("/inner", Value(BigInt(1)))))
    val updatedScore = new DateTime(2014, 2, 22, 14, 0, 0, 0).getMillis
    val taskPayloadUpdatedEvent = TaskPayloadUpdated(aggregateId, AggregateVersion(2), updateTime, payloadUpdate, None, Some(updatedScore))
    handleEvents(futureTaskCreatedEvent, taskPayloadUpdatedEvent)

    database.withDynTransaction {
      val task = readStore.retrieveBy(aggregateId)

      task must beEqualTo(Some(TestModel.readModels.waitingTask.copy(id = aggregateId, version = lastVersion, score = updatedScore, payload = updatedPayload)))
    }
  }

  "ignore duplicate events" in new context {
    handleEvents(taskCreatedEvent, taskStartedEvent, taskCreatedEvent)

    database.withDynTransaction {
      val task = readStore.retrieveBy(aggregateId)

      task must beEqualTo(Some(TestModel.readModels.startedTask.copy(id = aggregateId, version = lastVersion, payload = payload)))
    }
  }

  "archive task" in new context {
    handleEvents(taskCreatedEvent, archivedEvent)

    database.withDynTransaction {
      val task = readStore.retrieveBy(aggregateId)
      task must beNone
      sql"""SELECT id, data FROM archived WHERE id = ${aggregateId.value}""".as[String].firstOption.isDefined must beTrue
    }
  }

  "requeue task" in new context {
    handleEvents(taskCreatedEvent, taskStartedEvent, taskClosedEvent, taskRequeuedEvent)

    database.withDynTransaction {
      val task = readStore.retrieveBy(aggregateId)

      task must beEqualTo(Some(TestModel.readModels.readyTask.copy(
        id = aggregateId,
        version = taskRequeuedEvent.aggregateVersion,
        attempts = 1,
        payload = payload,
        previousStatus = Some(TestModel.previousStatus.copy(status = Closed)),
        assignee = Some(TestModel.assignee))))
    }
  }

  "ignore events on aggregate version mismatch" in new context {
    val unexpectedVersion = taskStartedEvent.aggregateVersion
    handleEvents(taskCreatedEvent, taskStartedEvent, archivedEvent.copy(aggregateVersion = unexpectedVersion))

    database.withDynTransaction {
      val task = readStore.retrieveBy(aggregateId)
      task must beSome(TestModel.readModels.startedTask.copy(id = aggregateId, version = lastVersion, payload = payload))
    }
  }
}
