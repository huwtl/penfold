package org.huwtl.penfold.app.store.jdbc

import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import Q.interpolation
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.app.support.json.EventSerializer
import org.huwtl.penfold.domain.event.Event
import org.huwtl.penfold.domain.store.EventStore
import java.sql.{SQLIntegrityConstraintViolationException, Timestamp}
import org.huwtl.penfold.domain.exceptions.AggregateConflictException

class JdbcEventStore(database: Database, eventSerializer: EventSerializer) extends EventStore {
  implicit val getEventFromRow = GetResult(row => eventSerializer.deserialize(row.nextString()))

  override def add(event: Event) {
    database.withDynSession {
      try {
        sqlu"""
        INSERT INTO events (type, aggregate_id, aggregate_version, aggregate_type, created, data) VALUES (
          ${event.getClass.getSimpleName},
          ${event.aggregateId.value},
          ${event.aggregateVersion.number},
          ${event.aggregateType.name},
          ${new Timestamp(event.created.getMillis).toString()},
          ${eventSerializer.serialize(event)}
        )
      """.execute
      }
      catch {
        case e: SQLIntegrityConstraintViolationException => throw new AggregateConflictException(s"aggregate conflict ${event.aggregateId}")
      }
    }
  }

  override def retrieveBy(aggregateId: AggregateId) = {
    database.withDynSession {
      sql"""
        SELECT data FROM events
          WHERE aggregate_id = ${aggregateId.value}
          ORDER BY aggregate_version
      """.as[Event].list
    }
  }
}