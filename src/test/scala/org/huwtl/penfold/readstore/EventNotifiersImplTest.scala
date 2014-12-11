package org.huwtl.penfold.readstore

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.mock.Mockito

class EventNotifiersImplTest extends Specification with Mockito {

  class context extends Scope {
    val eventNotifier1 = mock[EventNotifier]
    val eventNotifier2 = mock[EventNotifier]

    val notifiers = new EventNotifiersImpl(List(eventNotifier1, eventNotifier2))
  }

  "publish new events" in new context {
    notifiers.notifyAllOfEvents()

    there was one(eventNotifier1).notifyListener() andThen one(eventNotifier1).notifyListener()
    there was one(eventNotifier2).notifyListener() andThen one(eventNotifier2).notifyListener()
  }
}
