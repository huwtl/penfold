package org.huwtl.penfold.app.readstore.postgres

import scala.slick.driver.JdbcDriver.backend.Database
import scala.slick.jdbc.{StaticQuery => Q}

import org.huwtl.penfold.readstore._
import scala.Some
import org.huwtl.penfold.readstore.PageReference
import com.mongodb.casbah.Imports._
import org.huwtl.penfold.readstore.PageRequest
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.readstore.TaskRecord
import org.huwtl.penfold.app.readstore.mongodb.NavigationDirection.{Forward, Reverse}
import org.huwtl.penfold.app.readstore.mongodb.{LastKnownPageDetails, NavigationDirection, RestrictionField, QueryPlan}

class PaginatedQueryService(database: Database) {

  private val pageReferenceSeparator = "~"

  //lazy private val tasksCollection = database("tasks")

  def execQuery(queryPlan: QueryPlan, pageRequest: PageRequest, sortOrder: SortOrder): PageResult = {
//    val pageSize = pageRequest.pageSize
//
//    val criteria = buildPageQueryCriteria(queryPlan.restrictionFields)
//
//    parseLastKnownPageDetails(pageRequest.pageReference) match {
//      case Some(lastKnownPageDetails) => {
//        (lastKnownPageDetails.direction, sortOrder) match {
//          case (Forward, SortOrder.Desc) => queryForward(queryPlan, criteria, pageSize, lastKnownPageDetails, SortOrder.Desc, movingToLesserSortScores = true)
//          case (Forward, SortOrder.Asc) => queryForward(queryPlan, criteria, pageSize, lastKnownPageDetails, SortOrder.Asc, movingToLesserSortScores = false)
//          case (Reverse, SortOrder.Desc) => queryBackwards(queryPlan, criteria, pageSize, lastKnownPageDetails, SortOrder.Desc, movingToLesserSortScores = false)
//          case (Reverse, SortOrder.Asc) => queryBackwards(queryPlan, criteria, pageSize, lastKnownPageDetails, SortOrder.Asc, movingToLesserSortScores = true)
//        }
//      }
//      case None => {
//        val resultsWithOverflow = execPageQueryWithOverflow(criteria, sortCriteria(queryPlan, sortOrder), pageSize)
//        val results = resultsWithOverflow take pageSize
//        val nextPage = if (resultsWithOverflow.size > pageSize) pageReference(results, Forward) else None
//
//        PageResult(results, previousPage = None, nextPage = nextPage)
//      }
//    }
    null
  }

  private def buildPageQueryCriteria(restrictions: List[RestrictionField]) = {
//    val criteria = MongoDBObject.empty
//    restrictions.foldLeft(criteria)((previousCriteria, restriction) => {
//      restriction.filter match {
//        case EQ(key, value, dataType) => previousCriteria ++ MongoDBObject(restriction.path -> value)
//        case IN(key, values, dataType) => previousCriteria ++ (restriction.path $in values)
//        case LT(key, value, dataType) => previousCriteria ++ (restriction.path $lt Option(value).map(_.toLong).getOrElse(Long.MinValue))
//        case GT(key, value, dataType) => previousCriteria ++ (restriction.path $gt Option(value).map(_.toLong).getOrElse(Long.MaxValue))
//        case _ => throw new IllegalStateException("unsupported filter type")
//      }
//    })
    null
  }

  private def execPageQueryWithOverflow(criteria: MongoDBObject, sort: MongoDBObject, pageSize: Int) = {
//    if (pageSize > 0) {
//      tasksCollection.find(criteria).sort(sort).limit(pageSize + 1).map(taskMapper.mapDocumentToTask(_)).toList
//    }
//    else {
//      Nil
//    }
    null
  }

  private def enforcePageSortOrder(results: List[TaskRecord], sortOrder: SortOrder) = {
    sortOrder match {
      case SortOrder.Desc => results.sortWith((e1, e2) => e1.id.value > e2.id.value).sortWith((e1, e2) => e1.sort > e2.sort)
      case SortOrder.Asc => results.sortWith((e1, e2) => e1.id.value < e2.id.value).sortWith((e1, e2) => e1.sort < e2.sort)
    }
  }

  private def sortCriteriaForNavigation(queryPlan: QueryPlan, movingToLesserSortScores: Boolean) = {
    if (movingToLesserSortScores) sortCriteria(queryPlan, SortOrder.Desc) else sortCriteria(queryPlan, SortOrder.Asc)
  }

  private def sortCriteria(queryPlan: QueryPlan, sortOrder: SortOrder) = {
    val sortDirection = if (sortOrder == SortOrder.Desc) -1 else 1

    MongoDBObject(queryPlan.sortFields.map(_.path -> sortDirection))
  }

  private def parseLastKnownPageDetails(pageReference: Option[PageReference]) = {
//    if (pageReference.isDefined) {
//      pageReference.get.value.split(pageReferenceSeparator) match {
//        case Array(idFromLastViewedPage, sortValueFromLastViewedPage, navigationalDirection) => {
//          Some(LastKnownPageDetails(AggregateId(idFromLastViewedPage), sortValueFromLastViewedPage.toLong, if (navigationalDirection == "1") Forward else Reverse))
//        }
//        case _ => None
//      }
//    }
//    else {
//      None
//    }
    null
  }

  private def pageReference(results: List[TaskRecord], direction: NavigationDirection) = {
    direction match {
      case Forward => Some(PageReference(Array(results.last.id.value, results.last.sort, 1) mkString pageReferenceSeparator))
      case Reverse => Some(PageReference(Array(results.head.id.value, results.head.sort, 0) mkString pageReferenceSeparator))
    }
  }

  private def pagePositionRestriction(lastKnownDetails: LastKnownPageDetails, withLesserSortScore: Boolean) = {
    val sortMatch = MongoDBObject("sort" -> lastKnownDetails.sortValue)

    if (withLesserSortScore) {
      $or($and(sortMatch, "_id" $lt lastKnownDetails.id.value), "sort" $lt lastKnownDetails.sortValue)
    }
    else {
      $or($and(sortMatch, "_id" $gt lastKnownDetails.id.value), "sort" $gt lastKnownDetails.sortValue)
    }
  }

  private def queryForward(queryPlan: QueryPlan, criteria: MongoDBObject, pageSize: Int, lastKnownPageDetails: LastKnownPageDetails, sortOrder: SortOrder, movingToLesserSortScores: Boolean) = {
//    val skipForwardFromLastVisitedPage = pagePositionRestriction(lastKnownPageDetails, withLesserSortScore = movingToLesserSortScores)
//    val resultsWithOverflow = execPageQueryWithOverflow(criteria ++ skipForwardFromLastVisitedPage, sortCriteriaForNavigation(queryPlan, movingToLesserSortScores = movingToLesserSortScores), pageSize)
//    val results = enforcePageSortOrder(resultsWithOverflow take pageSize, sortOrder)
//    val previousPage = if (results.nonEmpty) pageReference(results, Reverse) else None
//    val nextPage = if (resultsWithOverflow.size > pageSize) pageReference(results, Forward) else None
//
//    PageResult(results, previousPage = previousPage, nextPage = nextPage)
    null
  }

  private def queryBackwards(queryPlan: QueryPlan, criteria: MongoDBObject, pageSize: Int, lastKnownPageDetails: LastKnownPageDetails, sortOrder: SortOrder, movingToLesserSortScores: Boolean) = {
//    val skipBackFromLastVisitedPage = pagePositionRestriction(lastKnownPageDetails, withLesserSortScore = movingToLesserSortScores)
//    val resultsWithOverflow = execPageQueryWithOverflow(criteria ++ skipBackFromLastVisitedPage, sortCriteriaForNavigation(queryPlan, movingToLesserSortScores = movingToLesserSortScores), pageSize)
//    val results = enforcePageSortOrder(resultsWithOverflow take pageSize, sortOrder)
//    val previousPage = if (resultsWithOverflow.size > pageSize) pageReference(results, Reverse) else None
//    val nextPage = if (results.nonEmpty) pageReference(results, Forward) else None
//
//    PageResult(results, previousPage = previousPage, nextPage = nextPage)
    null
  }
}
