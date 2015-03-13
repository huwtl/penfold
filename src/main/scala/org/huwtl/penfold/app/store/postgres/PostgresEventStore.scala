package org.huwtl.penfold.app.store.postgres

import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import Q.interpolation
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.app.support.json.EventSerializer
import org.huwtl.penfold.domain.event.Event
import org.huwtl.penfold.domain.store.EventStore
import java.sql.Timestamp
import org.huwtl.penfold.domain.exceptions.AggregateConflictException
import scala.util.{Failure, Success, Try}
import org.postgresql.util.PSQLException

class PostgresEventStore(database: Database, eventSerializer: EventSerializer) extends EventStore {
  implicit val getEventFromRow = GetResult(row => eventSerializer.deserialize(row.nextString()))

  private val connectionSuccess = true

  override def checkConnectivity = {
    Try(database.withDynSession(sql"""SELECT 1""".as[String].first)) match {
      case Success(_) => Left(connectionSuccess)
      case Failure(e: Exception) => Right(e)
      case Failure(e) => throw e
    }
  }

  override def add(event: Event) = {
    try {
      sqlu"""
        INSERT INTO events (type, version, aggregate_id, aggregate_version, aggregate_type, created, data) VALUES (
          ${event.getClass.getSimpleName},
          ${event.version},
          ${event.aggregateId.value},
          ${event.aggregateVersion.number},
          ${event.aggregateType.name},
          ${new Timestamp(event.created.getMillis)},
          ${eventSerializer.serialize(event)}
        )
        """.execute
      event
    } catch {
      case e: PSQLException if e.getSQLState == "23505" => throw new AggregateConflictException(s"aggregate conflict ${event.aggregateId}")
    }
  }

  override def retrieveBy(aggregateId: AggregateId) = {
    sql"""
        SELECT data FROM events
          WHERE aggregate_id = ${aggregateId.value}
          ORDER BY aggregate_version
      """.as[Event].list
  }
}