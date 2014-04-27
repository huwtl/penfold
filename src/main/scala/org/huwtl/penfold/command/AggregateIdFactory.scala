package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.AggregateId

trait AggregateIdFactory {
  def create: AggregateId
}
