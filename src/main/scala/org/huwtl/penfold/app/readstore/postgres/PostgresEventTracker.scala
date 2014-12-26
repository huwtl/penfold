package org.huwtl.penfold.app.readstore.postgres

import java.sql.SQLException

import grizzled.slf4j.Logger
import org.huwtl.penfold.readstore.{EventSequenceId, EventTracker, NextExpectedEventIdProvider}

import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{StaticQuery => Q}

class PostgresEventTracker(trackerKey: String, database: Database) extends EventTracker with NextExpectedEventIdProvider {
  private lazy val logger = Logger(getClass)

  private val dupSqlState = "23505"

  override def trackEvent(eventId: EventSequenceId) {
    database.withDynSession {
      try {
        val rowsUpdated = sqlu"""UPDATE trackers SET last_event_id = ${eventId.value} where id = $trackerKey and last_event_id < ${eventId.value}""".first

        if (rowsUpdated == 0 && !lastEventId.isDefined) {
          sqlu"""INSERT INTO trackers (id, last_event_id) VALUES ($trackerKey, ${eventId.value})""".execute
        }
      } catch {
        case e: SQLException if e.getSQLState == dupSqlState => {
          logger.info(s"tracking row already exists for event $eventId")
        }
      }
    }
  }

  override def nextExpectedEvent = {
    lastEventId match {
      case Some(lastId) => EventSequenceId(lastId + 1)
      case None => EventSequenceId.first
    }
  }

  private def lastEventId = {
    database.withDynSession {
      sql"""SELECT last_event_id FROM trackers WHERE id = $trackerKey"""
        .as[Long]
        .firstOption
    }
  }
}
