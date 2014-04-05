package org.huwtl.penfold.domain.store

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.query.EventNotifiers
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.Binding
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.domain.event.{JobTriggered, JobCreated}
import org.joda.time.DateTime
import org.specs2.specification.Scope

class DomainRepositoryTest extends Specification with Mockito {
  class context extends Scope {
    val aggregateId = AggregateId("a1")

    val binding = Binding(List(BoundQueue(QueueId("q1"))))

    val timestamp = DateTime.now

    val createdJob = Job.create(aggregateId, binding, Payload.empty)

    val eventStore = mock[EventStore]

    val notifiers = mock[EventNotifiers]

    val repo = new DomainRepository(eventStore, notifiers)
  }

  "append new aggregate root events to event store" in new context {
    val job = repo.add(createdJob)

    job.uncommittedEvents must beEmpty
    there was one(notifiers).notifyAllOfEvents()
  }

  "load aggregate by id" in new context {
    eventStore.retrieveBy(aggregateId) returns List(
      JobCreated(aggregateId, AggregateVersion.init, timestamp, binding, timestamp, Payload.empty),
      JobTriggered(aggregateId, AggregateVersion.init.next, timestamp, List(QueueId("q1")))
    )

    val job = repo.getById[Job](aggregateId)

    job.status must beEqualTo(Status.Ready)
  }

  "throw exception when no aggregate found with id" in new context {
    val unknownAggregateId = AggregateId("unknown")
    eventStore.retrieveBy(unknownAggregateId) returns Nil

    repo.getById[Job](AggregateId("unknown")) must throwA[IllegalArgumentException]
  }
}
