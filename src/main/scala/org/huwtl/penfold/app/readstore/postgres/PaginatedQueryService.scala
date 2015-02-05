package org.huwtl.penfold.app.readstore.postgres

import scala.slick.driver.JdbcDriver.backend.Database
import scala.slick.jdbc.{StaticQuery => Q}
import Database.dynamicSession
import Q.interpolation

import org.huwtl.penfold.readstore._
import scala.Some
import org.huwtl.penfold.readstore.PageReference
import org.huwtl.penfold.readstore.PageRequest
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.readstore.TaskRecord
import org.huwtl.penfold.app.readstore.mongodb.NavigationDirection.{Forward, Reverse}
import org.huwtl.penfold.app.readstore.mongodb.{LastKnownPageDetails, NavigationDirection, RestrictionField, QueryPlan}
import org.huwtl.penfold.app.support.json.ObjectSerializer

class PaginatedQueryService(database: Database, objectSerializer: ObjectSerializer) {

  private val pageReferenceSeparator = "~"

  def execQuery(queryPlan: QueryPlan, pageRequest: PageRequest, sortOrder: SortOrder): PageResult = {
    val pageSize = pageRequest.pageSize

    val criteria = buildPageQueryCriteria(queryPlan.restrictionFields)

    parseLastKnownPageDetails(pageRequest.pageReference) match {
      case Some(lastKnownPageDetails) => {
        (lastKnownPageDetails.direction, sortOrder) match {
          case (Forward, SortOrder.Desc) => queryForward(queryPlan, criteria, pageSize, lastKnownPageDetails, SortOrder.Desc, movingToLesserSortScores = true)
          case (Forward, SortOrder.Asc) => queryForward(queryPlan, criteria, pageSize, lastKnownPageDetails, SortOrder.Asc, movingToLesserSortScores = false)
          case (Reverse, SortOrder.Desc) => queryBackwards(queryPlan, criteria, pageSize, lastKnownPageDetails, SortOrder.Desc, movingToLesserSortScores = false)
          case (Reverse, SortOrder.Asc) => queryBackwards(queryPlan, criteria, pageSize, lastKnownPageDetails, SortOrder.Asc, movingToLesserSortScores = true)
        }
      }
      case None => {
        val resultsWithOverflow = execPageQueryWithOverflow(criteria, sortCriteria(sortOrder), pageSize)
        val results = resultsWithOverflow take pageSize
        val nextPage = if (resultsWithOverflow.size > pageSize) pageReference(results, Forward) else None

        PageResult(results, previousPage = None, nextPage = nextPage)
      }
    }
    null
  }

  private def buildPageQueryCriteria(restrictions: List[RestrictionField]) = {
    restrictions.foldLeft(List[String]())((previousCriteria, restriction) => {
      restriction.filter match {
        case EQ(key, value, dataType) => s"data->>'${restriction.path}' = $value" :: previousCriteria
        case IN(key, values, dataType) => s"data->>'${restriction.path}' IN $values" :: previousCriteria
        case LT(key, value, dataType) => s"data->>'${restriction.path}' < ${Option(value).map(_.toLong).getOrElse(Long.MinValue)}" :: previousCriteria
        case GT(key, value, dataType) => s"data->>'${restriction.path}' > ${Option(value).map(_.toLong).getOrElse(Long.MinValue)}" :: previousCriteria
        case _ => throw new IllegalStateException("unsupported filter type")
      }
    })
    null
  }

  private def execPageQueryWithOverflow(criteria: List[String], sort: String, pageSize: Int) = {
    if (pageSize > 0) {
      val criteriaStr = criteria mkString " AND "
      database.withDynSession {
        val rows = sql"""SELECT data FROM tasks WHERE $criteriaStr $sort""".as[String].list()
        rows.map(row => objectSerializer.deserialize[TaskData](row).toTaskRecord)
      }
    }
    else {
      List.empty
    }
  }

  private def enforcePageSortOrder(results: List[TaskRecord], sortOrder: SortOrder) = {
    sortOrder match {
      case SortOrder.Desc => results.sortWith((e1, e2) => e1.id.value > e2.id.value).sortWith((e1, e2) => e1.sort > e2.sort)
      case SortOrder.Asc => results.sortWith((e1, e2) => e1.id.value < e2.id.value).sortWith((e1, e2) => e1.sort < e2.sort)
    }
  }

  private def sortCriteriaForNavigation(queryPlan: QueryPlan, movingToLesserSortScores: Boolean) = {
    if (movingToLesserSortScores) sortCriteria(SortOrder.Desc) else sortCriteria(SortOrder.Asc)
  }

  private def sortCriteria(sortOrder: SortOrder) = {
    val sortDirection = if (sortOrder == SortOrder.Desc) "DESC" else "ASC"

    s"ORDER BY data->>'sort', id $sortDirection"
  }

  private def parseLastKnownPageDetails(pageReference: Option[PageReference]) = {
    if (pageReference.isDefined) {
      pageReference.get.value.split(pageReferenceSeparator) match {
        case Array(idFromLastViewedPage, sortValueFromLastViewedPage, navigationalDirection) =>
          Some(LastKnownPageDetails(AggregateId(idFromLastViewedPage), sortValueFromLastViewedPage.toLong, if (navigationalDirection == "1") Forward else Reverse))
        case _ => None
      }
    }
    else {
      None
    }
  }

  private def pageReference(results: List[TaskRecord], direction: NavigationDirection) = {
    direction match {
      case Forward => Some(PageReference(Array(results.last.id.value, results.last.sort, 1) mkString pageReferenceSeparator))
      case Reverse => Some(PageReference(Array(results.head.id.value, results.head.sort, 0) mkString pageReferenceSeparator))
    }
  }

  private def pagePositionRestriction(lastKnownDetails: LastKnownPageDetails, withLesserSortScore: Boolean) = {
    if (withLesserSortScore) {
      s"(((data->>'sort')::numeric = ${lastKnownDetails.sortValue} AND id < ${lastKnownDetails.id.value}) OR (data->>'sort')::numeric < ${lastKnownDetails.sortValue})"
    }
    else {
      s"(((data->>'sort')::numeric = ${lastKnownDetails.sortValue} AND id > ${lastKnownDetails.id.value}) OR (data->>'sort')::numeric > ${lastKnownDetails.sortValue})"
    }
  }

  private def queryForward(queryPlan: QueryPlan, criteria: List[String], pageSize: Int, lastKnownPageDetails: LastKnownPageDetails, sortOrder: SortOrder, movingToLesserSortScores: Boolean) = {
    val skipForwardFromLastVisitedPage = pagePositionRestriction(lastKnownPageDetails, withLesserSortScore = movingToLesserSortScores)
    val resultsWithOverflow = execPageQueryWithOverflow(criteria ++ skipForwardFromLastVisitedPage, sortCriteriaForNavigation(queryPlan, movingToLesserSortScores = movingToLesserSortScores), pageSize)
    val results = enforcePageSortOrder(resultsWithOverflow take pageSize, sortOrder)
    val previousPage = if (results.nonEmpty) pageReference(results, Reverse) else None
    val nextPage = if (resultsWithOverflow.size > pageSize) pageReference(results, Forward) else None

    PageResult(results, previousPage = previousPage, nextPage = nextPage)
  }

  private def queryBackwards(queryPlan: QueryPlan, criteria: List[String], pageSize: Int, lastKnownPageDetails: LastKnownPageDetails, sortOrder: SortOrder, movingToLesserSortScores: Boolean) = {
    val skipBackFromLastVisitedPage = pagePositionRestriction(lastKnownPageDetails, withLesserSortScore = movingToLesserSortScores)
    val resultsWithOverflow = execPageQueryWithOverflow(criteria ++ skipBackFromLastVisitedPage, sortCriteriaForNavigation(queryPlan, movingToLesserSortScores = movingToLesserSortScores), pageSize)
    val results = enforcePageSortOrder(resultsWithOverflow take pageSize, sortOrder)
    val previousPage = if (resultsWithOverflow.size > pageSize) pageReference(results, Reverse) else None
    val nextPage = if (results.nonEmpty) pageReference(results, Forward) else None

    PageResult(results, previousPage = previousPage, nextPage = nextPage)
  }
}
