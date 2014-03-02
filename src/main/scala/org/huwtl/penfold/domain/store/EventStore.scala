package org.huwtl.penfold.domain.store

import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.event.Event
import org.huwtl.penfold.domain.exceptions.EventConflictException

trait EventStore {
  def retrieveBy(id: AggregateId): List[Event]

  def add(event: Event): Unit
}
