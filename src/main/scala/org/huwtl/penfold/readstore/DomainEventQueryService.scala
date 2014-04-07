package org.huwtl.penfold.readstore

trait DomainEventQueryService {
  def retrieveBy(id: EventSequenceId): Option[EventRecord]

  def retrieveIdOfLast: Option[EventSequenceId]
}
