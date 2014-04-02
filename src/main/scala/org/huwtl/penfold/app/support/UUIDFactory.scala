package org.huwtl.penfold.app.support

import org.huwtl.penfold.command.support.AggregateIdFactory
import java.util.UUID
import org.huwtl.penfold.domain.model.AggregateId

class UUIDFactory extends AggregateIdFactory {
  override def create = AggregateId(UUID.randomUUID().toString)
}
