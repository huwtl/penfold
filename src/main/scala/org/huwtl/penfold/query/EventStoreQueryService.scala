package org.huwtl.penfold.query

trait EventStoreQueryService {
  def retrieveBy(id: EventSequenceId): Option[EventRecord]

  def retrieveIdOfLast: Option[EventSequenceId]
}
