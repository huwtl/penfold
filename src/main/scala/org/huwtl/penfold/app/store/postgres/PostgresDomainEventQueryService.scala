package org.huwtl.penfold.app.store.postgres

import org.huwtl.penfold.readstore.{EventRecord, EventSequenceId, DomainEventQueryService}

import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import Q.interpolation
import org.huwtl.penfold.app.support.json.EventSerializer
import org.huwtl.penfold.support.Retry.retryUntilSome
import grizzled.slf4j.Logger
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit.MILLISECONDS

class PostgresDomainEventQueryService(database: Database, serializer: EventSerializer, eventRetrievalRetries: Int = 50) extends DomainEventQueryService {
  private lazy val logger = Logger(getClass)

  implicit val getEventFromRow = GetResult(row => EventRecord(EventSequenceId(row.nextLong()), serializer.deserialize(row.nextString())))

  implicit val getEventIdFromRow = GetResult(row => EventSequenceId(row.nextLong()))

  override def retrieveIdOfLast = {
    database.withDynSession {
      sql"""
        SELECT id FROM events ORDER BY id DESC LIMIT 1
      """.as[EventSequenceId].firstOption
    }
  }

  override def retrieveBy(id: EventSequenceId) = {
    val foundEvent = database.withDynSession {
      retryUntilSome[EventRecord](retries = eventRetrievalRetries, interval = FiniteDuration(100, MILLISECONDS)) {
        sql"""SELECT id, data FROM events WHERE id = ${id.value}""".as[EventRecord].firstOption
      }
    }

    foundEvent match {
      case None => {
        logger.warn(s"assumed gap in consecutive event id numbering for missing event ${id.value}, skipping event")
        None
      }
      case some => some
    }
  }
}
