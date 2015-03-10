package org.huwtl.penfold.app.readstore.postgres

import org.huwtl.penfold.readstore.{EventSequenceId, EventTracker, NextExpectedEventIdProvider}

import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{StaticQuery => Q}

class PostgresEventTracker(trackerKey: String, database: Database) extends EventTracker with NextExpectedEventIdProvider {
  override def trackEvent(eventId: EventSequenceId) {
    val rowsUpdated = sqlu"""UPDATE trackers SET last_event_id = ${eventId.value} where id = $trackerKey and last_event_id < ${eventId.value}""".first

    if (rowsUpdated == 0 && !lastEventId.isDefined) {
      sqlu"""INSERT INTO trackers (id, last_event_id) VALUES ($trackerKey, ${eventId.value})""".execute
    }
  }

  override def nextExpectedEvent = {
    lastEventId match {
      case Some(lastId) => EventSequenceId(lastId + 1)
      case None => EventSequenceId.first
    }
  }

  private def lastEventId = {
    sql"""SELECT last_event_id FROM trackers WHERE id = $trackerKey"""
      .as[Long]
      .firstOption
  }
}
