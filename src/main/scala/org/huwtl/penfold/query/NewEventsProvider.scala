package org.huwtl.penfold.query

class NewEventsProvider(queryStoreNextExpectedEventProvider: NextExpectedEventIdProvider, eventStoreQueryRepository: EventStoreQueryRepository) {
  def newEvents: Stream[EventRecord] = {
    val nextExpectedEventId = queryStoreNextExpectedEventProvider.nextExpectedEvent
    val lastEventId = eventStoreQueryRepository.retrieveIdOfLast getOrElse EventSequenceId(-1)
    for {
      eventId <- (nextExpectedEventId.value.toInt to lastEventId.value.toInt).toStream
      event <- eventStoreQueryRepository.retrieveBy(EventSequenceId(eventId))
    } yield event
  }
}
