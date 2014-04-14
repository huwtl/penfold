package org.huwtl.penfold.app.readstore.mongodb

import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.readstore._
import com.mongodb.casbah.Imports._
import org.huwtl.penfold.domain.model._
import com.mongodb.util.JSON
import org.huwtl.penfold.readstore.PageRequest
import org.huwtl.penfold.domain.model.BoundQueue
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.Binding
import org.huwtl.penfold.readstore.TaskRecord
import org.huwtl.penfold.readstore.NavigationDirection.{Reverse, Forward}
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
      Binding(List(BoundQueue(QueueId(document.as[String]("queue"))))),
      Status.from(document.as[String]("status")).get,
      new DateTime(document.as[Date]("triggerDate")),
      document.as[Long]("sort"),
      objectSerializer.deserialize[Payload](JSON.serialize(document("payload")))
    )
  }

  private def retrievePage(criteria: MongoDBObject, pageRequest: PageRequest) = {
    def execPageQueryWithOverflow(criteria: MongoDBObject, sort: MongoDBObject, pageSize: Int) = {
      tasksCollection.find(criteria).sort(sort).limit(pageSize + 1).map(convertDocumentToTask(_)).toList
    }

    def sortPageInDescOrder(results: PageResult) = {
      results.copy(entries = results.entries.sortWith((e1, e2) => e1.id.value > e2.id.value).sortWith((e1, e2) => e1.sort > e2.sort))
    }

    val pageSize = pageRequest.pageSize
    val sortDesc = MongoDBObject("sort" -> -1, "_id" -> -1)
    val sortAsc = MongoDBObject("sort" -> 1, "_id" -> 1)

    pageRequest.lastKnownPageDetails match {
      case Some(lastKnownPageDetails) => {
        val scoreMatch = "sort" $eq lastKnownPageDetails.score

        lastKnownPageDetails.direction match {
          case Forward => {
            val skipForwardFromLastVisitedPage = $or($and(scoreMatch, "_id" $lt lastKnownPageDetails.id.value), "sort" $lt lastKnownPageDetails.score)
            val resultsWithOverflow = execPageQueryWithOverflow(criteria ++ skipForwardFromLastVisitedPage, sortDesc, pageSize)
            val results = resultsWithOverflow take pageSize

            PageResult(results, previousExists = results.nonEmpty, nextExists = resultsWithOverflow.size > pageSize)
          }
          case Reverse => {
            val skipBackFromLastVisitedPage = $or($and(scoreMatch, "_id" $gt lastKnownPageDetails.id.value), "sort" $gt lastKnownPageDetails.score)
            val resultsWithOverflow = execPageQueryWithOverflow(criteria ++ skipBackFromLastVisitedPage, sortAsc, pageSize)
            val results = resultsWithOverflow take pageSize

            sortPageInDescOrder(PageResult(results, previousExists = resultsWithOverflow.size > pageSize, nextExists = results.nonEmpty))
          }
        }
      }
      case None => {
        val resultsWithOverflow = execPageQueryWithOverflow(criteria, sortDesc, pageSize)
        val results = resultsWithOverflow take pageSize
        val nextExists = resultsWithOverflow.size > pageSize

        PageResult(results, previousExists = false, nextExists = nextExists)
      }
    }
  }
}
