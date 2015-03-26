package org.huwtl.penfold.domain.store

import org.huwtl.penfold.domain.model.{AggregateId, AggregateRoot}

trait DomainRepository {
  def getById[T <: AggregateRoot](id: AggregateId): T

  def add(aggregateRoot: AggregateRoot): AggregateRoot
}
