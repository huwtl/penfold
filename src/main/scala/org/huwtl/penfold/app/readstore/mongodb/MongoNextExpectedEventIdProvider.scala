package org.huwtl.penfold.app.readstore.mongodb

import org.huwtl.penfold.readstore.{EventSequenceId, NextExpectedEventIdProvider}
import scala.Some
import com.mongodb.casbah.Imports._

class MongoNextExpectedEventIdProvider(trackingKey: String, database: MongoDB) extends NextExpectedEventIdProvider {
  lazy private val trackers = database("eventTrackers")

  override def nextExpectedEvent = {
    val query = MongoDBObject("_id" -> trackingKey)

    trackers.findOne(query) match {
      case Some(tracker) => EventSequenceId(tracker.as[Long]("lastEventId") + 1)
      case None => EventSequenceId.first
    }
  }
}
