package org.huwtl.penfold.command.support

import org.huwtl.penfold.domain.model.AggregateId

trait AggregateIdFactory {
  def create: AggregateId
}
