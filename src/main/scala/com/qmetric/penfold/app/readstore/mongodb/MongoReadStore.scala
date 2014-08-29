package com.qmetric.penfold.app.readstore.mongodb

import com.qmetric.penfold.readstore._
import com.mongodb.casbah.Imports._
import com.qmetric.penfold.domain.model._
import com.qmetric.penfold.readstore.PageRequest
import com.qmetric.penfold.domain.model.QueueId
import com.qmetric.penfold.domain.model.AggregateId
import com.qmetric.penfold.domain.model.Status.Waiting
import scala.util.{Failure, Try, Success}
import com.qmetric.penfold.app.support.DateTimeSource
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers

class MongoReadStore(database: MongoDB, indexes: Indexes, taskMapper: MongoTaskMapper, paginatedQueryService: PaginatedQueryService, dateTimeSource: DateTimeSource) extends ReadStore {
  private val connectionSuccess = true

  lazy private val tasksCollection = database("tasks")

  RegisterJodaTimeConversionHelpers()

  override def checkConnectivity: Either[Boolean, Exception] = {
    Try(database.collectionNames()) match {
      case Success(_) => Left(connectionSuccess)
      case Failure(e: Exception) => Right(e)
      case Failure(e) => throw e
    }
  }

  override def retrieveTasksToTrigger: Iterator[TaskRecordReference] = {
    val currentTime = dateTimeSource.now

    val query = MongoDBObject("status" -> Waiting.name) ++ ("sort" $lte currentTime.getMillis)
    val sort = MongoDBObject("sort" -> 1)

    tasksCollection.find(query).sort(sort).map(taskMapper.mapDocumentToTaskReference(_))
  }

  override def retrieveTasksToTimeout(timeoutAttributePath: String, status: Option[Status] = None): Iterator[TaskRecordReference] = {
    val currentTime = dateTimeSource.now

    val query = status.map(status => MongoDBObject("status" -> status.name)).getOrElse(MongoDBObject.empty) ++ (timeoutAttributePath $lte currentTime.getMillis)

    tasksCollection.find(query).map(taskMapper.mapDocumentToTaskReference(_))
  }

  override def retrieveBy(id: AggregateId) = {
    val task = tasksCollection.findOne(MongoDBObject("_id" -> id.value)).map(taskMapper.mapDocumentToTask(_))
    task
  }

  override def retrieveByQueue(queueId: QueueId, status: Status, pageRequest: PageRequest, sortOrder: SortOrder, filters: Filters) = {
    val filtersWithQueueStatus = new Filters(EQ("queue", queueId.value) :: EQ("status", status.name) :: filters.all)
    retrieveByPage(filtersWithQueueStatus, pageRequest, sortOrder)
  }

  override def retrieveBy(filters: Filters, pageRequest: PageRequest) = {
    retrieveByPage(filters, pageRequest, SortOrder.Desc)
  }

  private def retrieveByPage(filters: Filters, pageRequest: PageRequest, sortOrder: SortOrder) = {
    val queryPlan = indexes.buildQueryPlan(filters)

    paginatedQueryService.execQuery(queryPlan, pageRequest, sortOrder)
  }
}
