package org.huwtl.penfold.query

import org.specs2.specification.Scope
import org.huwtl.penfold.domain.event.JobTriggered
import org.huwtl.penfold.domain.model.{Version, AggregateId}
import org.specs2.mock.Mockito
import org.specs2.matcher.DataTables
import org.specs2.mutable.Specification

class NewEventsProviderTest extends Specification with Mockito with DataTables {
  class context extends Scope {
    val event1 = EventRecord(EventSequenceId(0), JobTriggered(AggregateId("1"), Version.init, List()))
    val event2 = EventRecord(EventSequenceId(1), JobTriggered(AggregateId("2"), Version.init, List()))

    val nextExpectedEventIdProvider = mock[NextExpectedEventIdProvider]

    val eventStoreQueryRepository = mock[EventStoreQueryService]
    eventStoreQueryRepository.retrieveBy(event1.id) returns Some(event1)
    eventStoreQueryRepository.retrieveBy(event2.id) returns Some(event2)

    val newEventsProvider = new NewEventsProvider(nextExpectedEventIdProvider, eventStoreQueryRepository)
  }

  "provide new events" in new context {
    "lastEventId"            | "nextExpectedId"   | "expectedStream"       |
    None                     ! EventSequenceId(0) ! Stream.empty           |
    Some(EventSequenceId(0)) ! EventSequenceId(0) ! Stream(event1)         |
    Some(EventSequenceId(1)) ! EventSequenceId(0) ! Stream(event1, event2) |
    Some(EventSequenceId(1)) ! EventSequenceId(1) ! Stream(event2)         |> {
      (lastEventId, nextExpectedId, expectedStream) =>
        eventStoreQueryRepository.retrieveIdOfLast returns lastEventId
        nextExpectedEventIdProvider.nextExpectedEvent returns nextExpectedId

        newEventsProvider.newEvents must beEqualTo(expectedStream)
    }
  }
}
