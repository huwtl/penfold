package org.huwtl.penfold.domain.store

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.readstore.EventNotifiers
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.domain.event.{TaskTriggered, TaskCreated}
import org.joda.time.DateTime
import org.specs2.specification.Scope

class DomainRepositoryImplTest extends Specification with Mockito {
  class context extends Scope {
    val aggregateId = AggregateId("a1")

    val binding = QueueBinding(QueueId("q1"))

    val timestamp = DateTime.now

    val createdTask = Task.create(aggregateId, binding, Payload.empty, None)

    val eventStore = mock[EventStore]

    val notifiers = mock[EventNotifiers]

    val repo = new DomainRepositoryImpl(eventStore, notifiers)
  }

  "append new aggregate root events to event store" in new context {
    val task = repo.add(createdTask)

    task.uncommittedEvents must beEmpty
    there was one(notifiers).notifyAllOfEvents()
  }

  "load aggregate by id" in new context {
    eventStore.retrieveBy(aggregateId) returns List(
      TaskCreated(aggregateId, AggregateVersion.init, timestamp, binding, timestamp, Payload.empty, timestamp.getMillis),
      TaskTriggered(aggregateId, AggregateVersion.init.next, timestamp)
    )

    val task = repo.getById[Task](aggregateId)

    task.status must beEqualTo(Status.Ready)
  }

  "throw exception when no aggregate found with id" in new context {
    val unknownAggregateId = AggregateId("unknown")
    eventStore.retrieveBy(unknownAggregateId) returns Nil

    repo.getById[Task](AggregateId("unknown")) must throwA[IllegalArgumentException]
  }
}
