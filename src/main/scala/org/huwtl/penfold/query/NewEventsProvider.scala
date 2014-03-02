package org.huwtl.penfold.query

class NewEventsProvider(nextExpectedEventProvider: NextExpectedEventIdProvider, eventStoreQueryService: EventStoreQueryService) {
  def newEvents: Stream[EventRecord] = {
    val nextExpectedEventId = nextExpectedEventProvider.nextExpectedEvent
    val lastEventId = eventStoreQueryService.retrieveIdOfLast getOrElse EventSequenceId(-1)
    for {
      eventId <- (nextExpectedEventId.value.toInt to lastEventId.value.toInt).toStream
      event <- eventStoreQueryService.retrieveBy(EventSequenceId(eventId))
    } yield event
  }
}
