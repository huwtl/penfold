package org.huwtl.penfold.app.schedule

import org.huwtl.penfold.readstore.EventNotifiers
import scala.concurrent.duration.FiniteDuration
import scala.slick.driver.JdbcDriver.backend.Database

class EventSyncScheduler(notifiers: EventNotifiers, database: Database, override val frequency: FiniteDuration) extends Scheduler {
  override val name = "event sync"

  override def process() {
    database.withDynSession {
      notifiers.notifyAllOfEvents()
    }
  }
}
