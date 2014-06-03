package com.qmetric.penfold.readstore

trait DomainEventQueryService {
  def retrieveBy(id: EventSequenceId): Option[EventRecord]

  def retrieveIdOfLast: Option[EventSequenceId]
}
