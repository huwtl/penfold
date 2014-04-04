package org.huwtl.penfold.query

trait DomainEventQueryService {
  def retrieveBy(id: EventSequenceId): Option[EventRecord]

  def retrieveIdOfLast: Option[EventSequenceId]
}
