package com.qmetric.penfold.app.support

import java.util.UUID
import com.qmetric.penfold.domain.model.AggregateId
import com.qmetric.penfold.command.AggregateIdFactory

class UUIDFactory extends AggregateIdFactory {
  override def create = AggregateId(UUID.randomUUID().toString)
}
