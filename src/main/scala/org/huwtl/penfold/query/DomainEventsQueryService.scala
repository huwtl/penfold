package org.huwtl.penfold.query

trait DomainEventsQueryService {
  def retrieveBy(id: EventSequenceId): Option[EventRecord]

  def retrieveIdOfLast: Option[EventSequenceId]
}
