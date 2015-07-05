package org.huwtl.penfold.readstore

import org.huwtl.penfold.domain.event.Event
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

class EventNotifierTest extends SpecificationWithJUnit with Mockito {

  class context extends Scope {
    val eventRecord1 = mock[Event]
    val eventRecord2 = mock[Event]
    val eventListener = mock[EventListener]
    val eventNotifier = new EventNotifier(eventListener)
  }

  "notify listener of new events" in new context {
    eventNotifier.notify(List(eventRecord1, eventRecord2))

    there was one(eventListener).handle(eventRecord1)
    there was one(eventListener).handle(eventRecord2)
  }
}
