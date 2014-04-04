package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.AggregateType

trait JobEvent extends Event {
  override val aggregateType = AggregateType.Job
}









