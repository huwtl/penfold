package org.huwtl.penfold.app.readstore.mongodb

import org.specs2.mutable.Specification
import com.github.athieriot.EmbedConnection
import org.specs2.specification.Scope
import com.mongodb.casbah.Imports._
import org.huwtl.penfold.readstore.EventSequenceId

class MongoEventTrackerTest extends Specification with EmbedConnection {
  sequential

  class context extends Scope {
    val trackingKey = "testKey"
    val mongoClient = MongoClient("localhost", embedConnectionPort())
    val database = mongoClient("penfoldtest")
    database("eventTrackers").dropCollection()
    val tracker = new MongoEventTracker(trackingKey, database)
    val nextExpectedEventIdProvider = new MongoNextExpectedEventIdProvider(trackingKey, database)
  }

  "know first expected event id when no events tracked" in new context {
    nextExpectedEventIdProvider.nextExpectedEvent must beEqualTo(EventSequenceId.first)
  }

  "track single event as being handled" in new context {
    tracker.trackEvent(EventSequenceId.first)

    nextExpectedEventIdProvider.nextExpectedEvent must beEqualTo(EventSequenceId(1))
  }

  "track multiple events as being handled" in new context {
    tracker.trackEvent(EventSequenceId(0))
    tracker.trackEvent(EventSequenceId(1))
    tracker.trackEvent(EventSequenceId(2))

    nextExpectedEventIdProvider.nextExpectedEvent must beEqualTo(EventSequenceId(3))
  }

  "ignore requests to track previously tracked events" in new context {
    tracker.trackEvent(EventSequenceId(0))
    tracker.trackEvent(EventSequenceId(1))
    tracker.trackEvent(EventSequenceId(0))

    nextExpectedEventIdProvider.nextExpectedEvent must beEqualTo(EventSequenceId(2))
  }
}
