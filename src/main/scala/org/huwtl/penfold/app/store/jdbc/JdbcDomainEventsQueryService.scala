package org.huwtl.penfold.app.store.jdbc

import org.huwtl.penfold.query.{EventRecord, EventSequenceId, DomainEventsQueryService}

import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import Q.interpolation
import org.huwtl.penfold.app.support.json.EventSerializer

class JdbcDomainEventsQueryService(database: Database, serializer: EventSerializer) extends DomainEventsQueryService {
  implicit val getEventFromRow = GetResult(row => EventRecord(EventSequenceId(row.nextLong()), serializer.deserialize(row.nextString())))

  implicit val getEventIdFromRow = GetResult(row => EventSequenceId(row.nextLong()))

  override def retrieveIdOfLast = {
    database.withDynSession {
      sql"""
        SELECT id FROM events
          ORDER BY id DESC LIMIT 1
      """.as[EventSequenceId].firstOption
    }
  }

  override def retrieveBy(id: EventSequenceId) = {
    database.withDynSession {
      sql"""
        SELECT id, data FROM events
          WHERE id = ${id.value}
      """.as[EventRecord].firstOption
    }
  }
}
