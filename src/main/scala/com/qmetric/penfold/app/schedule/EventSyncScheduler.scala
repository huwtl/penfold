package com.qmetric.penfold.app.schedule

import com.qmetric.penfold.readstore.EventNotifiers
import scala.concurrent.duration.FiniteDuration

class EventSyncScheduler(notifiers: EventNotifiers, override val frequency: FiniteDuration) extends Scheduler {
  override val name = "event sync"

  override def process() {
    notifiers.notifyAllOfEvents()
  }
}
