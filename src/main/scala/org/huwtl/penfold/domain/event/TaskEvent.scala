package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.AggregateType

trait TaskEvent extends Event {
  override val aggregateType = AggregateType.Task
}









