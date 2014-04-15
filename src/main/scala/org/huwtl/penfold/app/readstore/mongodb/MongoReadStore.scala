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

class MongoReadStore(database: MongoDB, objectSerializer: ObjectSerializer, dateTimeSource: DateTimeSource) extends ReadStore {
  private val connectionSuccess = true

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
    val criteria = MongoDBObject("queue" -> queueId.value, "status" -> status.name) ++ MongoDBObject(filters.all.map(f => f.key -> f.value))

    retrievePage(criteria, pageRequest)
  }

  override def retrieveBy(filters: Filters, pageRequest: PageRequest) = {
    val criteria = MongoDBObject(filters.all.map(f => f.key -> f.value))

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
        val scoreMatch = "sort" $eq lastKnownPageDetails.score

        lastKnownPageDetails.direction match {
          case Forward => {
            val skipForwardFromLastVisitedPage = $or($and(scoreMatch, "_id" $lt lastKnownPageDetails.id.value), "sort" $lt lastKnownPageDetails.score)
            val resultsWithOverflow = execPageQueryWithOverflow(criteria ++ skipForwardFromLastVisitedPage, sortDesc, pageSize)
            val results = resultsWithOverflow take pageSize
            val previousPage = if (results.nonEmpty) pageReference(results, Reverse) else None
            val nextPage = if (resultsWithOverflow.size > pageSize) pageReference(results, Forward) else None

            PageResult(results, previousPage = previousPage, nextPage = nextPage)
          }
          case Reverse => {
            val skipBackFromLastVisitedPage = $or($and(scoreMatch, "_id" $gt lastKnownPageDetails.id.value), "sort" $gt lastKnownPageDetails.score)
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
      pageReference.get.value.split('~') match {
        case Array(idFromLastViewedPage, scoreFromLastViewedPage, navigationalDirection) => {
          Some(LastKnownPageDetails(AggregateId(idFromLastViewedPage), scoreFromLastViewedPage.toLong, if (navigationalDirection == "1") Forward else Reverse))
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
      Some(PageReference(s"${results.last.id.value}~${results.last.sort}~${1}"))
    }
    else {
      Some(PageReference(s"${results.head.id.value}~${results.head.sort}~${0}"))
    }

  }
}
