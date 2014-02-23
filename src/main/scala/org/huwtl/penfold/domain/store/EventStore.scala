package org.huwtl.penfold.domain.store

import org.huwtl.penfold.domain.model.Id
import org.huwtl.penfold.domain.event.Event
import org.huwtl.penfold.domain.exceptions.EventConflictException

trait EventStore {
  def getByAggregateId(id: Id): List[Event]

  def add(event: Event): Unit
}
