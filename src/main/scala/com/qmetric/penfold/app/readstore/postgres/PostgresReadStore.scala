package com.qmetric.penfold.app.readstore.postgres

import com.qmetric.penfold.readstore._
import scala.util.{Failure, Success, Try}
import com.qmetric.penfold.app.support.DateTimeSource
import com.qmetric.penfold.domain.model.Status.Waiting
import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.slick.jdbc.{StaticQuery => Q}
import Q.interpolation
import com.qmetric.penfold.domain.model.{QueueId, AggregateId, Status}
import com.qmetric.penfold.app.support.json.ObjectSerializer
import scala.concurrent.duration.FiniteDuration

class PostgresReadStore(database: Database, paginatedQueryService: PaginatedQueryService, objectSerializer: ObjectSerializer, dateTimeSource: DateTimeSource, aliases: Aliases) extends ReadStore {
  private val connectionSuccess = true

  override def checkConnectivity: Either[Boolean, Exception] = {
    Try(database.withDynSession(sql"""SELECT 1""".as[String].first)) match {
      case Success(_) => Left(connectionSuccess)
      case Failure(e: Exception) => Right(e)
      case Failure(e) => throw e
    }
  }

  override def retrieveBy(id: AggregateId) = {
    database.withDynSession {
      val json = sql"""SELECT data FROM tasks WHERE id = ${id.value}""".as[String].firstOption
      val taskData = json.map(objectSerializer.deserialize[TaskData])
      taskData.map(_.toTaskProjection)
    }
  }

  override def retrieveByQueue(queueId: QueueId, status: Status, pageRequest: PageRequest, sortOrder: SortOrder, filters: Filters) = {
    val filtersWithQueueStatus = new Filters(EQ("queue", queueId.value) :: EQ("status", status.name) :: filters.all)
    retrieveByPage(filtersWithQueueStatus, pageRequest, sortOrder)
  }

  override def retrieveBy(filters: Filters, pageRequest: PageRequest) = {
      retrieveByPage(filters, pageRequest, SortOrder.Desc)
  }

  override def forEachTriggeredTask(f: TaskProjectionReference => Unit) {
    database.withDynSession {
      val currentTime = dateTimeSource.now.getMillis

      val rows = sql"""SELECT data FROM tasks WHERE data->>'status' = ${Waiting.name} AND (data->>'triggerDate')::bigint <= $currentTime ORDER BY data->>'triggerDate'""".as[String].iterator
      rows.foreach(row => f(objectSerializer.deserialize[TaskData](row).toTaskProjectionReference))
    }
  }

  override def forEachTimedOutTask(status: Status, timeoutDuration: FiniteDuration, f: TaskProjectionReference => Unit) {
    database.withDynSession {
      val timeout = dateTimeSource.now.getMillis - timeoutDuration.toMillis

      val rows = sql"""SELECT data FROM tasks WHERE data->>'status' = ${status.name} AND (data->>'statusLastModified')::bigint <= $timeout ORDER BY data->>'statusLastModified'""".as[String].iterator
      rows.foreach(row => {
        f(objectSerializer.deserialize[TaskData](row).toTaskProjectionReference)
      })
    }
  }

  private def retrieveByPage(filters: Filters, pageRequest: PageRequest, sortOrder: SortOrder) = {
    database.withDynSession {
      paginatedQueryService.execQuery(filters, pageRequest, sortOrder)
    }
  }
}
