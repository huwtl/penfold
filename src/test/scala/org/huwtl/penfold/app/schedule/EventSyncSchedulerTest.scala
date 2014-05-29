package org.huwtl.penfold.app.schedule

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.readstore.EventNotifiers

class EventSyncSchedulerTest extends Specification with Mockito {

  "periodically ensure read store is in sync with event store" in {
    val eventNotifiers = mock[EventNotifiers]

    new EventSyncScheduler(eventNotifiers, null).process()

    there was one(eventNotifiers).notifyAllOfEvents()
  }
}
