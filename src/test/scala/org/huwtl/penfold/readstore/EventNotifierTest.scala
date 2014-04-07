package org.huwtl.penfold.readstore

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.Scope

class EventNotifierTest extends Specification with Mockito {

  class context extends Scope {
    val eventRecord1 = mock[EventRecord]
    val eventRecord2 = mock[EventRecord]
    val newEventsProvider = mock[NewEventsProvider]
    val eventListener = mock[EventListener]
    val eventNotifier = new EventNotifier(newEventsProvider, eventListener)
  }

  "notify listener of new events" in new context {
    newEventsProvider.newEvents returns List(eventRecord1, eventRecord2).toStream

    eventNotifier.notifyListener()

    there was one(eventListener).handle(eventRecord1)
    there was one(eventListener).handle(eventRecord2)
  }

  "skip any further new events once event handling fails for one event" in new context {
    newEventsProvider.newEvents returns List(eventRecord1, eventRecord2).toStream
    eventListener.handle(eventRecord1) throws new RuntimeException

    eventNotifier.notifyListener()

    there was one(eventListener).handle(eventRecord1)
    there was no(eventListener).handle(eventRecord2)
  }
}
