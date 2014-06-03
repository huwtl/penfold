package com.qmetric.penfold.domain.event

import com.qmetric.penfold.domain.model.AggregateType

trait TaskEvent extends Event {
  override val aggregateType = AggregateType.Task
}









