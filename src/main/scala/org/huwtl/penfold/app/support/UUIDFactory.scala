package org.huwtl.penfold.app.support

import java.util.UUID
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.command.AggregateIdFactory

class UUIDFactory extends AggregateIdFactory {
  override def create = AggregateId(UUID.randomUUID().toString)
}
