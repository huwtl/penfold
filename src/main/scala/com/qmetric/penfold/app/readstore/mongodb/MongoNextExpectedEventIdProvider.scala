package com.qmetric.penfold.app.readstore.mongodb

import com.qmetric.penfold.readstore.{EventSequenceId, NextExpectedEventIdProvider}
import scala.Some
import com.mongodb.casbah.Imports._

class MongoNextExpectedEventIdProvider(trackerKey: String, database: MongoDB) extends NextExpectedEventIdProvider {
  lazy private val trackers = database("eventTrackers")

  override def nextExpectedEvent = {
    val query = MongoDBObject("_id" -> trackerKey)

    trackers.findOne(query) match {
      case Some(tracker) => EventSequenceId(tracker.as[Long]("lastEventId") + 1)
      case None => EventSequenceId.first
    }
  }
}
