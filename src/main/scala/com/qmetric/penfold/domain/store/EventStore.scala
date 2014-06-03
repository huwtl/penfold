package com.qmetric.penfold.domain.store

import com.qmetric.penfold.domain.model.AggregateId
import com.qmetric.penfold.domain.event.Event

trait EventStore {
  def checkConnectivity: Either[Boolean, Exception]

  def retrieveBy(id: AggregateId): List[Event]

  def add(event: Event): Event
}
