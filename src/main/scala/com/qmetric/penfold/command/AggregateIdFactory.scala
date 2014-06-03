package com.qmetric.penfold.command

import com.qmetric.penfold.domain.model.AggregateId

trait AggregateIdFactory {
  def create: AggregateId
}
