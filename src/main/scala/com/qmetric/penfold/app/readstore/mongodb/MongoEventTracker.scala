package com.qmetric.penfold.app.readstore.mongodb

import com.qmetric.penfold.readstore.{EventSequenceId, EventTracker}
import com.mongodb.casbah.Imports._
import com.mongodb.DuplicateKeyException
import grizzled.slf4j.Logger

class MongoEventTracker(trackerKey: String, database: MongoDB) extends EventTracker {
  private lazy val logger = Logger(getClass)

  lazy private val trackers = database("eventTrackers")

  override def trackEvent(eventId: EventSequenceId) = {
    val query = MongoDBObject("_id" -> trackerKey) ++ ("lastEventId" $lt eventId.value)

    val tracking = MongoDBObject("_id" -> trackerKey, "lastEventId" -> eventId.value)

    try {
      trackers.update(query, tracking, upsert = true)
    }
    catch {
      case e: DuplicateKeyException => logger.debug(s"ignoring tracking for already handled event ${eventId.value}")
      case e: Exception => throw e
    }
  }
}
