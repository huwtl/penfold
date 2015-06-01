package com.qmetric.penfold.app.store.jdbc

import com.qmetric.penfold.readstore.{EventRecord, EventSequenceId, DomainEventQueryService}

import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import Q.interpolation
import com.qmetric.penfold.app.support.json.EventSerializer
import com.qmetric.penfold.support.Retry.retryUntilSome
import grizzled.slf4j.Logger
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit.MILLISECONDS

class JdbcDomainEventQueryService(database: Database, serializer: EventSerializer, eventRetrievalRetries: Int = 50) extends DomainEventQueryService {
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
    // Includes retries to verify a gap in consecutive event ids. It is possible that the event is momentarily not visible due to a race condition between threads.
    // For example, in mysql, auto increment columns are incremented before transactions, meaning two inserts at the
    // same time might get persisted in a different order compared with their auto increment values, such as where event(id = 5) might get persisted before event(id = 4).
    // Please note that under very heavy load, events might still be skipped being mistaken as a gap in consecutive id numbering.
    // todo: More effective solutions, below:
    // 1) Add table locking around insert of every event - possible performance impact.
    // 2) Ideal solution would be to process events in a more flexible order, only requiring that events of any given aggregate root are processed in order,
    // i.e. currently, events for a given aggregate root are persisted in order in the event log due to agg versioning constraints - assuming there are no dependencies between agg roots.

    val foundEvent = database.withDynSession {
      retryUntilSome[EventRecord](retries = eventRetrievalRetries, interval = FiniteDuration(100, MILLISECONDS)) {
        sql"""SELECT id, data FROM events WHERE id = ${id.value}""".as[EventRecord].firstOption
      }
    }

    foundEvent match {
      case None => {
        logger.info(s"assumed gap in consecutive event id numbering for missing event ${id.value}, skipping event")
        None
      }
      case some => some
    }
  }
}
