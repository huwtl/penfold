package org.huwtl.penfold.query

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.mock.Mockito

class NewEventsPublisherTest extends Specification with Mockito {

  class context extends Scope {
    val newEventNotifier1 = mock[NewEventsNotifier]
    val newEventNotifier2 = mock[NewEventsNotifier]

    val newEventPublisher = new NewEventsPublisher(List(newEventNotifier1, newEventNotifier2))
  }

  "publish new events" in new context {
    newEventPublisher.publishNewEvents()

    there was one(newEventNotifier1).notifyListener() andThen one(newEventNotifier1).notifyListener()
    there was one(newEventNotifier2).notifyListener() andThen one(newEventNotifier2).notifyListener()
  }
}
