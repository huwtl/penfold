package org.huwtl.penfold.domain.store

import org.huwtl.penfold.domain.model.{Task, AggregateId, AggregateRoot}
import org.huwtl.penfold.readstore.EventNotifiers

class DomainRepository(eventStore: EventStore, notifiers: EventNotifiers) {
  def getById[T <: AggregateRoot](id: AggregateId): T = {
    eventStore.retrieveBy(id) match {
      case Nil => throw new IllegalArgumentException(s"${id.value} does not exist")
      case events => Task.loadFromHistory[T](events)
    }
  }

  def add(aggregateRoot: AggregateRoot): AggregateRoot = {
    val uncommittedEvents = aggregateRoot.uncommittedEvents.reverse
    uncommittedEvents.foreach (eventStore.add)

    notifiers.notifyAllOfEvents()

    aggregateRoot.markCommitted
  }
}
