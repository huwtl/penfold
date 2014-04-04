package org.huwtl.penfold.domain.store

import org.huwtl.penfold.domain.model.{Job, AggregateId, AggregateRoot}
import org.huwtl.penfold.query.NewEventsPublisher

class DomainRepository(eventStore: EventStore, eventPublisher: NewEventsPublisher) {
  def getById[T <: AggregateRoot](id: AggregateId): T = {
    eventStore.retrieveBy(id) match {
      case Nil => throw new IllegalArgumentException(s"${id.value} does not exist")
      case events => Job.loadFromHistory[T](events)
    }
  }

  def add(aggregateRoot: AggregateRoot): AggregateRoot = {
    val uncommittedEvents = aggregateRoot.uncommittedEvents.reverse
    uncommittedEvents.foreach (eventStore.add)

    eventPublisher.publishNewEvents()

    aggregateRoot.markCommitted
  }
}
