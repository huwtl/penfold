package org.huwtl.penfold.app.readstore.postgres

import org.huwtl.penfold.readstore._
import scala.util.{Failure, Success, Try}
import org.huwtl.penfold.app.support.DateTimeSource
import org.huwtl.penfold.domain.model.Status.Waiting
import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.slick.jdbc.{StaticQuery => Q}
import Q.interpolation
import org.huwtl.penfold.domain.model.{QueueId, AggregateId, Status}
import org.huwtl.penfold.app.support.json.ObjectSerializer

class PostgresReadStore(database: Database, paginatedQueryService: PaginatedQueryService, objectSerializer: ObjectSerializer, dateTimeSource: DateTimeSource) extends ReadStore {
  private val connectionSuccess = true

  override def checkConnectivity: Either[Boolean, Exception] = {
    Try(database.withDynSession(sql"""SELECT 1""".as[String].first)) match {
      case Success(_) => Left(connectionSuccess)
      case Failure(e: Exception) => Right(e)
      case Failure(e) => throw e
    }
  }

  override def retrieveBy(id: AggregateId) = {
    val taskData = database.withDynSession {
      val json = sql"""SELECT data FROM tasks WHERE id = ${id.value}""".as[String].firstOption
      json.map(objectSerializer.deserialize[TaskData])
    }
    taskData.map(_.toTaskRecord)
  }

  override def forEachTriggeredTask(f: TaskRecord => Unit) {
    val currentTime = dateTimeSource.now

    database.withDynSession {
      val rows = sql"""SELECT data FROM tasks WHERE data->>'status' = ${Waiting.name} AND (data->>'sort')::numeric <= ${currentTime.getMillis} ORDER BY data->>'sort'""".as[String].iterator()
      rows.foreach(row => f(objectSerializer.deserialize[TaskData](row).toTaskRecord))
    }
  }

  override def retrieveTasksToTrigger(): Iterator[TaskRecordReference] = {
    null
  }

  override def retrieveTasksToTimeout(timeoutAttributePath: String, status: Option[Status] = None): Iterator[TaskRecordReference] = {
//    val currentTime = dateTimeSource.now
//
//    val query = status.map(status => MongoDBObject("status" -> status.name)).getOrElse(MongoDBObject.empty) ++ (timeoutAttributePath $lte currentTime.getMillis)
//
//    tasksCollection.find(query).map(taskMapper.mapDocumentToTaskReference(_))
    null
  }

  override def retrieveByQueue(queueId: QueueId, status: Status, pageRequest: PageRequest, sortOrder: SortOrder, filters: Filters) = {
    val filtersWithQueueStatus = new Filters(EQ("queue", queueId.value) :: EQ("status", status.name) :: filters.all)
    retrieveByPage(filtersWithQueueStatus, pageRequest, sortOrder)
  }

  override def retrieveBy(filters: Filters, pageRequest: PageRequest) = {
    retrieveByPage(filters, pageRequest, SortOrder.Desc)
  }

  private def retrieveByPage(filters: Filters, pageRequest: PageRequest, sortOrder: SortOrder) = {
//    val queryPlan = indexes.buildQueryPlan(filters)
//
//    paginatedQueryService.execQuery(queryPlan, pageRequest, sortOrder)
    null
  }
}
