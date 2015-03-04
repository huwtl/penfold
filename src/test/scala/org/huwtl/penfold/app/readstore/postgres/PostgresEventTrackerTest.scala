package org.huwtl.penfold.app.readstore.postgres

import org.huwtl.penfold.readstore.EventSequenceId
import org.huwtl.penfold.support.PostgresSpecification
import org.specs2.specification.Scope

import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.slick.jdbc.StaticQuery.interpolation

class PostgresEventTrackerTest extends PostgresSpecification {
  sequential

  val database = newDatabase()

  class context extends Scope {
    val trackingKey = "testKey"

    database.withDynSession {
      sqlu"""DELETE FROM trackers""".execute
    }

    val tracker = new PostgresEventTracker(trackingKey, database)
  }

  "know first expected event id when no events tracked" in new context {
    tracker.nextExpectedEvent must beEqualTo(EventSequenceId.first)
  }

  "track single event as being handled" in new context {
    tracker.trackEvent(EventSequenceId.first)

    tracker.nextExpectedEvent must beEqualTo(EventSequenceId(2))
  }

  "track multiple events as being handled" in new context {
    tracker.trackEvent(EventSequenceId(1))
    tracker.trackEvent(EventSequenceId(2))
    tracker.trackEvent(EventSequenceId(3))

    tracker.nextExpectedEvent must beEqualTo(EventSequenceId(4))
  }

  "ignore requests to track previously tracked events" in new context {
    tracker.trackEvent(EventSequenceId(1))
    tracker.trackEvent(EventSequenceId(2))
    tracker.trackEvent(EventSequenceId(1))

    tracker.nextExpectedEvent must beEqualTo(EventSequenceId(3))
  }

  "ignore same tracking of previously tracked event" in new context {
    tracker.trackEvent(EventSequenceId(1))
    tracker.trackEvent(EventSequenceId(1))

    tracker.nextExpectedEvent must beEqualTo(EventSequenceId(2))
  }
}
