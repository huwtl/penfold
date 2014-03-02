package org.huwtl.penfold.query

trait EventStoreQueryRepository {
  def retrieveBy(id: EventSequenceId): Option[EventRecord]

  def retrieveIdOfLast: Option[EventSequenceId]
}
