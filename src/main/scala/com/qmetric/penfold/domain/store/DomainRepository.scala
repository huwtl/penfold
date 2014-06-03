package com.qmetric.penfold.domain.store

import com.qmetric.penfold.domain.model.{Task, AggregateId, AggregateRoot}
import com.qmetric.penfold.readstore.EventNotifiers

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
