package org.huwtl.penfold.domain.store

import org.huwtl.penfold.domain.model.{Job, AggregateId, AggregateRoot}
import org.huwtl.penfold.query.NewEventPublisher

class DomainRepository(eventStore: EventStore, eventPublisher: NewEventPublisher) {
  def getById[T <: AggregateRoot](id: AggregateId): Option[T] = {
    Some(Job.loadFromHistory[T](eventStore.retrieveBy(id)))
  }

  def add(aggregateRoot: AggregateRoot): AggregateRoot = {
    val uncommittedEvents = aggregateRoot.uncommittedEvents.reverse
    uncommittedEvents.foreach (eventStore.add)

    eventPublisher.publishNewEvents()

    aggregateRoot.markCommitted
  }
}
