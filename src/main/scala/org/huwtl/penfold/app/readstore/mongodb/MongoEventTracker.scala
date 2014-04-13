package org.huwtl.penfold.app.readstore.mongodb

import org.huwtl.penfold.readstore.{EventSequenceId, EventTracker}
import com.mongodb.casbah.Imports._

class MongoEventTracker(trackerKey: String, database: MongoDB) extends EventTracker {
  lazy private val trackers = database("eventTrackers")

  override def trackEvent(eventId: EventSequenceId) = {
    val query = MongoDBObject("_id" -> trackerKey)

    val tracking = MongoDBObject("lastEventId" -> eventId.value)

    trackers.update(query, tracking, upsert = true)
  }
}
