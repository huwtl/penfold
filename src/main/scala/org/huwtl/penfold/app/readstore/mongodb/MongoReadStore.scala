package org.huwtl.penfold.app.readstore.mongodb

import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.readstore._
import com.mongodb.casbah.Imports._
import org.huwtl.penfold.domain.model._
import com.mongodb.util.JSON
import org.huwtl.penfold.readstore.PageRequest
import org.huwtl.penfold.domain.model.QueueBinding
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.readstore.TaskRecord
import NavigationDirection.{Reverse, Forward}
import org.huwtl.penfold.domain.model.Status.Waiting
import org.joda.time.DateTime
import java.util.Date
import scala.util.{Failure, Try, Success}
import org.huwtl.penfold.app.support.DateTimeSource

class MongoReadStore(database: MongoDB, indexes: Indexes, objectSerializer: ObjectSerializer, dateTimeSource: DateTimeSource) extends ReadStore {
  private val connectionSuccess = true

  private val pageReferenceSeparator = "~"

  lazy private val tasksCollection = database("tasks")

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

  override def retrieveByQueue(queueId: QueueId, status: Status, pageRequest: PageRequest, filters: Filters) = {
    val criteria = appendFiltersToCriteria(MongoDBObject("queue" -> queueId.value, "status" -> status.name), indexes.transformForSuitableIndex(filters))

    retrievePage(criteria, pageRequest)
  }

  override def retrieveBy(filters: Filters, pageRequest: PageRequest) = {
    val criteria = appendFiltersToCriteria(MongoDBObject.empty, indexes.transformForSuitableIndex(filters))

    retrievePage(criteria, pageRequest)
  }

  override def retrieveBy(id: AggregateId) = {
    tasksCollection.findOne(MongoDBObject("_id" -> id.value)).map(convertDocumentToTask(_))
  }

  private def convertDocumentToTask(document: MongoDBObject) = {
    TaskRecord(
      AggregateId(document.as[String]("_id")),
      new DateTime(document.as[Date]("created")),
      QueueBinding(QueueId(document.as[String]("queue"))),
      Status.from(document.as[String]("status")).get,
      new DateTime(document.as[Date]("statusLastModified")),
      new DateTime(document.as[Date]("triggerDate")),
      document.as[Long]("sort"),
      objectSerializer.deserialize[Payload](JSON.serialize(document("payload")))
    )
  }

  private def retrievePage(criteria: MongoDBObject, pageRequest: PageRequest) = {
    def execPageQueryWithOverflow(criteria: MongoDBObject, sort: MongoDBObject, pageSize: Int) = {
      if (pageSize > 0) {
        tasksCollection.find(criteria).sort(sort).limit(pageSize + 1).map(convertDocumentToTask(_)).toList
      }
      else {
        Nil
      }
    }

    def sortPageInDescOrder(results: List[TaskRecord]) = {
      results.sortWith((e1, e2) => e1.id.value > e2.id.value).sortWith((e1, e2) => e1.sort > e2.sort)
    }

    val pageSize = pageRequest.pageSize
    val sortDesc = MongoDBObject("sort" -> -1, "_id" -> -1)
    val sortAsc = MongoDBObject("sort" -> 1, "_id" -> 1)

    parseLastKnownPageDetails(pageRequest.pageReference) match {
      case Some(lastKnownPageDetails) => {
        val sortMatch = MongoDBObject("sort" -> lastKnownPageDetails.sortValue)

        lastKnownPageDetails.direction match {
          case Forward => {
            val skipForwardFromLastVisitedPage = $or($and(sortMatch, "_id" $lt lastKnownPageDetails.id.value), "sort" $lt lastKnownPageDetails.sortValue)
            val resultsWithOverflow = execPageQueryWithOverflow(criteria ++ skipForwardFromLastVisitedPage, sortDesc, pageSize)
            val results = resultsWithOverflow take pageSize
            val previousPage = if (results.nonEmpty) pageReference(results, Reverse) else None
            val nextPage = if (resultsWithOverflow.size > pageSize) pageReference(results, Forward) else None

            PageResult(results, previousPage = previousPage, nextPage = nextPage)
          }
          case Reverse => {
            val skipBackFromLastVisitedPage = $or($and(sortMatch, "_id" $gt lastKnownPageDetails.id.value), "sort" $gt lastKnownPageDetails.sortValue)
            val resultsWithOverflow = execPageQueryWithOverflow(criteria ++ skipBackFromLastVisitedPage, sortAsc, pageSize)
            val results = sortPageInDescOrder(resultsWithOverflow take pageSize)
            val previousPage = if (resultsWithOverflow.size > pageSize) pageReference(results, Reverse) else None
            val nextPage = if (results.nonEmpty) pageReference(results, Forward) else None

            PageResult(results, previousPage = previousPage, nextPage = nextPage)
          }
        }
      }
      case None => {
        val resultsWithOverflow = execPageQueryWithOverflow(criteria, sortDesc, pageSize)
        val results = resultsWithOverflow take pageSize
        val nextPage = if (resultsWithOverflow.size > pageSize) pageReference(results, Forward) else None

        PageResult(results, previousPage = None, nextPage = nextPage)
      }
    }
  }

  private def parseLastKnownPageDetails(pageReference: Option[PageReference]) = {
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

  private def pageReference(results: List[TaskRecord], direction: NavigationDirection) = {
    if (direction == Forward) {
      Some(PageReference(Array(results.last.id.value, results.last.sort, 1) mkString pageReferenceSeparator))
    }
    else {
      Some(PageReference(Array(results.head.id.value, results.head.sort, 0) mkString pageReferenceSeparator))
    }
  }

  private def appendFiltersToCriteria(criteria: MongoDBObject, filters: Filters) = {
    filters.filters.foldLeft(criteria)((previousFilters, filter) => {
      val values = filter.values.map(nullWhenNone)
      previousFilters ++ (if (filter.isMulti) filter.key $in values else MongoDBObject(filter.key -> values.head))
    })
  }

  private def nullWhenNone(value: Option[String]) = value getOrElse null
}
