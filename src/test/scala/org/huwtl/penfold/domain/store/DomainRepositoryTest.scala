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

class DomainRepositoryTest extends Specification with Mockito {
  val aggregateId = AggregateId("a1")

  val binding = Binding(List(BoundQueue(QueueId("q1"))))

  val timestamp = DateTime.now

  val eventStore = mock[EventStore]

  val notifiers = mock[EventNotifiers]

  val repo = new DomainRepository(eventStore, notifiers)

  "append new aggregate root events to event store" in {
    val job = repo.add(createdJob)

    job.uncommittedEvents must beEmpty
    there was one(notifiers).notifyAllOfEvents()
  }

  "load aggregate by id" in {
    eventStore.retrieveBy(aggregateId) returns List(
      JobCreated(aggregateId, AggregateVersion.init, timestamp, binding, timestamp, Payload.empty),
      JobTriggered(aggregateId, AggregateVersion.init.next, timestamp, List(QueueId("q1")))
    )

    val job = repo.getById[Job](aggregateId)

    job.status must beEqualTo(Status.Ready)
  }

  private def createdJob = Job.create(aggregateId, binding, Payload.empty)
}
