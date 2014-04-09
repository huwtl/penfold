package org.huwtl.penfold.readstore

class NewEventsProvider(nextExpectedEventProvider: NextExpectedEventIdProvider, eventStoreQueryService: DomainEventQueryService) {
  def newEvents: Stream[EventRecord] = {
    val nextExpectedEventId = nextExpectedEventProvider.nextExpectedEvent
    val lastEventId = eventStoreQueryService.retrieveIdOfLast getOrElse EventSequenceId(-1)

    for {
      eventId <- (nextExpectedEventId.value to lastEventId.value).toStream
      event <- eventStoreQueryService.retrieveBy(EventSequenceId(eventId))
    } yield event
  }
}
