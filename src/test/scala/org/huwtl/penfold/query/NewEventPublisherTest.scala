package org.huwtl.penfold.query

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.mock.Mockito

class NewEventPublisherTest extends Specification with Mockito {

  class context extends Scope {
    val event1 = mock[EventRecord]
    val event2 = mock[EventRecord]

    val newEventsProvider = mock[NewEventsProvider]

    val newEventListener1 = mock[NewEventListener]
    val newEventListener2 = mock[NewEventListener]

    val newEventPublisher = new NewEventPublisher(newEventsProvider, List(newEventListener1, newEventListener2))
  }

  "publish new events" in new context {
    newEventsProvider.newEvents returns Stream(event1, event2)

    newEventPublisher.publishNewEvents()

    there was one(newEventListener1).handle(event1) andThen one(newEventListener1).handle(event2)
    there was one(newEventListener2).handle(event1) andThen one(newEventListener2).handle(event2)
  }

  "publish nothing when no new events" in new context {
    newEventsProvider.newEvents returns Stream.empty

    newEventPublisher.publishNewEvents()

    there was no(newEventListener1).handle(any[EventRecord])
    there was no(newEventListener2).handle(any[EventRecord])
  }
}
