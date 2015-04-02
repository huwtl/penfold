package com.qmetric.penfold.domain.store

import com.qmetric.penfold.domain.model.{AggregateId, AggregateRoot}

trait DomainRepository {
  def getById[T <: AggregateRoot](id: AggregateId): T

  def add(aggregateRoot: AggregateRoot): AggregateRoot
}
