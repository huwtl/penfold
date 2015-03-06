package org.huwtl.penfold.domain.store

import org.huwtl.penfold.domain.model.{Task, AggregateId, AggregateRoot}
import org.huwtl.penfold.readstore.EventNotifiers

trait DomainRepository {
  def getById[T <: AggregateRoot](id: AggregateId): T

  def add(aggregateRoot: AggregateRoot): AggregateRoot
}
