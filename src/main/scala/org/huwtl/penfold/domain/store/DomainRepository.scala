package org.huwtl.penfold.domain.store

import org.huwtl.penfold.domain.model.{Job, Id, AggregateRoot}
import org.huwtl.penfold.query.QueryStoreUpdater

class DomainRepository(eventStore: EventStore, queryStoreUpdater: QueryStoreUpdater) {
  def getById[T <: AggregateRoot](id: Id): Option[T] = {
    Some(Job.loadFromHistory[T](eventStore.getByAggregateId(id)))
  }

  def add(aggregateRoot: AggregateRoot): AggregateRoot = {
    val uncommittedEvents = aggregateRoot.uncommittedEvents.reverse
    uncommittedEvents.foreach (eventStore.add)

    queryStoreUpdater.updateWithNewEvents()

    aggregateRoot.markCommitted
  }
}
