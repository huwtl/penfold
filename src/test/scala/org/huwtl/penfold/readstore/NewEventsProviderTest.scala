package org.huwtl.penfold.readstore

import org.specs2.specification.Scope
import org.huwtl.penfold.domain.event.TaskTriggered
import org.huwtl.penfold.domain.model.{AggregateVersion, AggregateId}
import org.specs2.mock.Mockito
import org.specs2.matcher.DataTables
import org.specs2.mutable.Specification
import org.joda.time.DateTime

class NewEventsProviderTest extends Specification with Mockito with DataTables {
  class context extends Scope {
    val event1 = EventRecord(EventSequenceId(0), TaskTriggered(AggregateId("1"), AggregateVersion.init, DateTime.now))
    val event2 = EventRecord(EventSequenceId(1), TaskTriggered(AggregateId("2"), AggregateVersion.init, DateTime.now))

    val nextExpectedEventIdProvider = mock[NextExpectedEventIdProvider]

    val eventStoreQueryRepository = mock[DomainEventQueryService]
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
