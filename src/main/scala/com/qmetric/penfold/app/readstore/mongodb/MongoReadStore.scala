package com.qmetric.penfold.app.readstore.mongodb

import com.qmetric.penfold.app.support.json.ObjectSerializer
import com.qmetric.penfold.readstore._
import com.mongodb.casbah.Imports._
import com.qmetric.penfold.domain.model._
import com.mongodb.util.JSON
import com.qmetric.penfold.readstore.PageRequest
import com.qmetric.penfold.domain.model.QueueBinding
import com.qmetric.penfold.domain.model.QueueId
import com.qmetric.penfold.domain.model.AggregateId
import com.qmetric.penfold.readstore.TaskRecord
import com.qmetric.penfold.app.readstore.mongodb.NavigationDirection.{Forward, Reverse}
import com.qmetric.penfold.domain.model.Status.Waiting
import org.joda.time.DateTime
import scala.util.{Failure, Try, Success}
import com.qmetric.penfold.app.support.DateTimeSource
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.mongodb.casbah.commons.Imports

class MongoReadStore(database: MongoDB, indexes: Indexes, objectSerializer: ObjectSerializer, dateTimeSource: DateTimeSource) extends ReadStore {
  private val connectionSuccess = true

  private val pageReferenceSeparator = "~"

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

    tasksCollection.find(query).sort(sort).map(doc => TaskRecordReference(AggregateId(doc.as[String]("_id"))))
  }

  override def retrieveTasksToArchive(timeoutAttributePath: String): Iterator[TaskRecordReference] = {
    val currentTime = dateTimeSource.now

    val query = MongoDBObject.empty ++ (timeoutAttributePath $lte currentTime.getMillis)

    tasksCollection.find(query).map(doc => TaskRecordReference(AggregateId(doc.as[String]("_id"))))
  }

  override def retrieveBy(id: AggregateId) = {
    val task = tasksCollection.findOne(MongoDBObject("_id" -> id.value)).map(convertDocumentToTask(_))
    task
  }

  override def retrieveByQueue(queueId: QueueId, status: Status, pageRequest: PageRequest, sortOrder: SortOrder, filters: Filters) = {
    val filtersWithQueueStatus = new Filters(Filter("queue", Some(queueId.value)) :: Filter("status", Some(status.name)) :: filters.all)
    retrieveByPage(filtersWithQueueStatus, pageRequest, sortOrder)
  }

  override def retrieveBy(filters: Filters, pageRequest: PageRequest) = {
    retrieveByPage(filters, pageRequest, SortOrder.Desc)
  }

  private def retrieveByPage(filters: Filters, pageRequest: PageRequest, sortOrder: SortOrder) = {
    val queryPlan = indexes.buildQueryPlan(filters)
    retrievePage(queryPlan, pageRequest, sortOrder)
  }

  private def convertDocumentToTask(document: MongoDBObject) = {
    def parsePreviousStatus = document.getAs[Map[String, Any]]("previousStatus") match {
      case Some(previousStatus) => objectSerializer.deserialize[Option[PreviousStatus]](objectSerializer.serialize(previousStatus))
      case None => None
    }

    TaskRecord(
      AggregateId(document.as[String]("_id")),
      AggregateVersion(document.as[Int]("version")),
      document.as[DateTime]("created"),
      QueueBinding(QueueId(document.as[String]("queue"))),
      Status.from(document.as[String]("status")).get,
      document.as[DateTime]("statusLastModified"),
      parsePreviousStatus,
      document.getAs[String]("assignee").map(Assignee),
      document.as[DateTime]("triggerDate"),
      document.as[Long]("score"),
      document.as[Long]("sort"),
      objectSerializer.deserialize[Payload](JSON.serialize(document("payload"))),
      document.getAs[String]("rescheduleType"),
      document.getAs[String]("conclusionType")
    )
  }

  private def retrievePage(queryPlan: QueryPlan, pageRequest: PageRequest, sortOrder: SortOrder) = {
    def buildCriteria(restrictions: List[RestrictionField]) = {
      val criteria = MongoDBObject.empty
      restrictions.foldLeft(criteria)((previousCriteria, restriction) => {
        val values = restriction.values.map(_ getOrElse null)
        previousCriteria ++ (if (restriction.isMulti) restriction.name $in values else MongoDBObject(restriction.name -> values.head))
      })
    }

    def execPageQueryWithOverflow(criteria: MongoDBObject, sort: MongoDBObject, pageSize: Int) = {
      if (pageSize > 0) {
        tasksCollection.find(criteria).sort(sort).limit(pageSize + 1).map(convertDocumentToTask(_)).toList
      }
      else {
        Nil
      }
    }

    def enforcePageSortOrder(results: List[TaskRecord], sortOrder: SortOrder) = {
      sortOrder match {
        case SortOrder.Desc => results.sortWith((e1, e2) => e1.id.value > e2.id.value).sortWith((e1, e2) => e1.sort > e2.sort)
        case SortOrder.Asc => results.sortWith((e1, e2) => e1.id.value < e2.id.value).sortWith((e1, e2) => e1.sort < e2.sort)
      }
    }

    def sortCriteriaForNavigation(movingToLesserSortScores: Boolean) = {
      if (movingToLesserSortScores) sortCriteria(SortOrder.Desc) else sortCriteria(SortOrder.Asc)
    }

    def sortCriteria(sortOrder: SortOrder) = {
      val sortDirection = if (sortOrder == SortOrder.Desc) -1 else 1

      MongoDBObject(queryPlan.sortFields.map(_.name -> sortDirection))
    }

    def parseLastKnownPageDetails(pageReference: Option[PageReference]) = {
      if (pageReference.isDefined) {
        pageReference.get.value.split(pageReferenceSeparator) match {
          case Array(idFromLastViewedPage, sortValueFromLastViewedPage, navigationalDirection) => {
            Some(LastKnownPageDetails(AggregateId(idFromLastViewedPage), sortValueFromLastViewedPage.toLong, if (navigationalDirection == "1") Forward else Reverse))
          }
          case _ => None
        }
      }
      else {
        None
      }
    }

    def pageReference(results: List[TaskRecord], direction: NavigationDirection) = {
      direction match {
        case Forward => Some(PageReference(Array(results.last.id.value, results.last.sort, 1) mkString pageReferenceSeparator))
        case Reverse => Some(PageReference(Array(results.head.id.value, results.head.sort, 0) mkString pageReferenceSeparator))
      }
    }

    def pagePositionRestriction(lastKnownDetails: LastKnownPageDetails, withLesserSortScore: Boolean) = {
      val sortMatch = MongoDBObject("sort" -> lastKnownDetails.sortValue)

      if (withLesserSortScore) {
        $or($and(sortMatch, "_id" $lt lastKnownDetails.id.value), "sort" $lt lastKnownDetails.sortValue)
      }
      else {
        $or($and(sortMatch, "_id" $gt lastKnownDetails.id.value), "sort" $gt lastKnownDetails.sortValue)
      }
    }

    def queryForward(criteria: Imports.DBObject, pageSize: Int, lastKnownPageDetails: LastKnownPageDetails, sortOrder: SortOrder, movingToLesserSortScores: Boolean) = {
      val skipForwardFromLastVisitedPage = pagePositionRestriction(lastKnownPageDetails, withLesserSortScore = movingToLesserSortScores)
      val resultsWithOverflow = execPageQueryWithOverflow(criteria ++ skipForwardFromLastVisitedPage, sortCriteriaForNavigation(movingToLesserSortScores = movingToLesserSortScores), pageSize)
      val results = enforcePageSortOrder(resultsWithOverflow take pageSize, sortOrder)
      val previousPage = if (results.nonEmpty) pageReference(results, Reverse) else None
      val nextPage = if (resultsWithOverflow.size > pageSize) pageReference(results, Forward) else None

      PageResult(results, previousPage = previousPage, nextPage = nextPage)
    }

    def queryBackwards(criteria: Imports.DBObject, pageSize: Int, lastKnownPageDetails: LastKnownPageDetails, sortOrder: SortOrder, movingToLesserSortScores: Boolean) = {
      val skipBackFromLastVisitedPage = pagePositionRestriction(lastKnownPageDetails, withLesserSortScore = movingToLesserSortScores)
      val resultsWithOverflow = execPageQueryWithOverflow(criteria ++ skipBackFromLastVisitedPage, sortCriteriaForNavigation(movingToLesserSortScores = movingToLesserSortScores), pageSize)
      val results = enforcePageSortOrder(resultsWithOverflow take pageSize, sortOrder)
      val previousPage = if (resultsWithOverflow.size > pageSize) pageReference(results, Reverse) else None
      val nextPage = if (results.nonEmpty) pageReference(results, Forward) else None

      PageResult(results, previousPage = previousPage, nextPage = nextPage)
    }

    val criteria = buildCriteria(queryPlan.restrictionFields)

    val pageSize = pageRequest.pageSize

    parseLastKnownPageDetails(pageRequest.pageReference) match {
      case Some(lastKnownPageDetails) => {
        (lastKnownPageDetails.direction, sortOrder) match {
          case (Forward, SortOrder.Desc) => queryForward(criteria, pageSize, lastKnownPageDetails, SortOrder.Desc, movingToLesserSortScores = true)
          case (Forward, SortOrder.Asc) => queryForward(criteria, pageSize, lastKnownPageDetails, SortOrder.Asc, movingToLesserSortScores = false)
          case (Reverse, SortOrder.Desc) => queryBackwards(criteria, pageSize, lastKnownPageDetails, SortOrder.Desc, movingToLesserSortScores = false)
          case (Reverse, SortOrder.Asc) => queryBackwards(criteria, pageSize, lastKnownPageDetails, SortOrder.Asc, movingToLesserSortScores = true)
        }
      }
      case None => {
        val resultsWithOverflow = execPageQueryWithOverflow(criteria, sortCriteria(sortOrder), pageSize)
        val results = resultsWithOverflow take pageSize
        val nextPage = if (resultsWithOverflow.size > pageSize) pageReference(results, Forward) else None

        PageResult(results, previousPage = None, nextPage = nextPage)
      }
    }
  }
}
