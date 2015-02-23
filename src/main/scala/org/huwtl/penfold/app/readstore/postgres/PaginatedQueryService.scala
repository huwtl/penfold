package org.huwtl.penfold.app.readstore.postgres

import scala.slick.driver.JdbcDriver.backend.Database

import scala.slick.jdbc.{StaticQuery => Q, GetResult}
import org.huwtl.penfold.readstore._
import scala.Some
import org.huwtl.penfold.readstore.PageReference
import org.huwtl.penfold.readstore.PageRequest
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.readstore.TaskRecord
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.app.readstore.postgres.NavigationDirection.{Forward, Reverse}
import com.github.tminglei.slickpg._

import MyPostgresDriver.simple._

import scala.slick.lifted


class PaginatedQueryService(database: Database, objectSerializer: ObjectSerializer) {

  private val pageReferenceSeparator = "~"

  case class JsonBean(id: String, json: JsonString)

  class TestTable(tag: lifted.Tag) extends Table[JsonBean](tag, "tasks") {
    def id = column[String]("id", O.DBType("varchar(36)"), O.PrimaryKey)

    def data = column[JsonString]("data")

    def * = (id, data) <> (JsonBean.tupled, JsonBean.unapply)
  }
  val Tasks = TableQuery[TestTable]

  def execQuery(queryPlan: PostgresQueryPlan, pageRequest: PageRequest, sortOrder: SortOrder): PageResult = {
    val pageSize = pageRequest.pageSize

    val criteria = queryPlan.restrictionFields

    parseLastKnownPageDetails(pageRequest.pageReference) match {
      case Some(lastKnownPageDetails) =>
        (lastKnownPageDetails.direction, sortOrder) match {
          case (Forward, SortOrder.Desc) => queryForward(queryPlan, List()/*criteria*/, pageSize, lastKnownPageDetails, SortOrder.Desc, movingToLesserSortScores = true)
          case (Forward, SortOrder.Asc) => queryForward(queryPlan, List()/*criteria*/, pageSize, lastKnownPageDetails, SortOrder.Asc, movingToLesserSortScores = false)
          case (Reverse, SortOrder.Desc) => queryBackwards(queryPlan, List()/*criteria*/, pageSize, lastKnownPageDetails, SortOrder.Desc, movingToLesserSortScores = false)
          case (Reverse, SortOrder.Asc) => queryBackwards(queryPlan, List()/*criteria*/, pageSize, lastKnownPageDetails, SortOrder.Asc, movingToLesserSortScores = true)
        }
      case None =>
        val resultsWithOverflow = execPageQueryWithOverflow(criteria, lastKnownPageDetails, pageSize)
        val results = resultsWithOverflow take pageSize
        val nextPage = if (resultsWithOverflow.size > pageSize) pageReference(results, Forward) else None

        PageResult(results, previousPage = None, nextPage = nextPage)
    }
  }

  private def execPageQueryWithOverflow(criteria: List[PostgresRestrictionField], lastKnownPageDetails: LastKnownPageDetails, withLesserSortScore: Boolean, pageSize: Int, sortOrder: SortOrder) = {
    if (pageSize > 0) {
      database.withDynSession {
        val query: lifted.Query[TestTable, TestTable#TableElementType, Seq] = criteria.foldLeft(pagePositionRestriction(lastKnownPageDetails, withLesserSortScore))((query, restriction) => {
          query.filter(row =>
          restriction.filter match {
            case EQ(key, value, dataType) => row.data.+>>(restriction.path).asColumnOf[Long] === value.bind
            case IN(key, values, dataType) => row.data.+>>(restriction.path) inSetBind values
            case LT(key, value, dataType) => row.data.+>>(restriction.path).asColumnOf[Long] < Option(value).map(_.toLong).getOrElse(Long.MinValue).bind
            case GT(key, value, dataType) => row.data.+>>(restriction.path).asColumnOf[Long] > Option(value).map(_.toLong).getOrElse(Long.MaxValue).bind
            case _ => throw new IllegalStateException("unsupported filter type")
          })
        })

        val sort = sortCriteria(query, sortOrder)

        val rows = sort.map(_.data).take(pageSize + 1).list

        rows.map(row => objectSerializer.deserialize[TaskData](row.value).toTaskRecord)
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

  private def sortCriteriaForNavigation(queryPlan: PostgresQueryPlan, movingToLesserSortScores: Boolean) = {
    if (movingToLesserSortScores) sortCriteria(SortOrder.Desc) else sortCriteria(SortOrder.Asc)
  }

  private def sortCriteria(query: lifted.Query[TestTable, TestTable#TableElementType, Seq], sortOrder: SortOrder) = {
    if (sortOrder == SortOrder.Desc) query.sortBy(_.data.+>>("sort".desc).asColumnOf[Long]).sortBy(_.id) else query.sortBy(_.data.+>>("sort".asc).asColumnOf[Long]).sortBy(_.id)
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
      Tasks.filter(row => {
        (row.data.+>>("sort").asColumnOf[Long] === lastKnownDetails.sortValue.bind && row.id < lastKnownDetails.id.value.bind) || row.data.+>>("sort").asColumnOf[Long] < lastKnownDetails.sortValue.bind
      })
    }
    else {
      Tasks.filter(row => {
        (row.data.+>>("sort").asColumnOf[Long] === lastKnownDetails.sortValue.bind && row.id > lastKnownDetails.id.value.bind) || row.data.+>>("sort").asColumnOf[Long] > lastKnownDetails.sortValue.bind
      })
    }
  }

  private def queryForward(queryPlan: PostgresQueryPlan, criteria: List[String], pageSize: Int, lastKnownPageDetails: LastKnownPageDetails, sortOrder: SortOrder, movingToLesserSortScores: Boolean) = {
//    val skipForwardFromLastVisitedPage = pagePositionRestriction(lastKnownPageDetails, withLesserSortScore = movingToLesserSortScores)
//    val resultsWithOverflow = execPageQueryWithOverflow(skipForwardFromLastVisitedPage ::: criteria, sortCriteriaForNavigation(queryPlan, movingToLesserSortScores = movingToLesserSortScores), pageSize)
//    val results = enforcePageSortOrder(resultsWithOverflow take pageSize, sortOrder)
//    val previousPage = if (results.nonEmpty) pageReference(results, Reverse) else None
//    val nextPage = if (resultsWithOverflow.size > pageSize) pageReference(results, Forward) else None
//
//    PageResult(results, previousPage = previousPage, nextPage = nextPage)
    PageResult.empty
  }

  private def queryBackwards(queryPlan: PostgresQueryPlan, criteria: List[String], pageSize: Int, lastKnownPageDetails: LastKnownPageDetails, sortOrder: SortOrder, movingToLesserSortScores: Boolean) = {
//    val skipBackFromLastVisitedPage = pagePositionRestriction(lastKnownPageDetails, withLesserSortScore = movingToLesserSortScores)
//    val resultsWithOverflow = execPageQueryWithOverflow(skipBackFromLastVisitedPage ::: criteria, sortCriteriaForNavigation(queryPlan, movingToLesserSortScores = movingToLesserSortScores), pageSize)
//    val results = enforcePageSortOrder(resultsWithOverflow take pageSize, sortOrder)
//    val previousPage = if (resultsWithOverflow.size > pageSize) pageReference(results, Reverse) else None
//    val nextPage = if (results.nonEmpty) pageReference(results, Forward) else None
//
//    PageResult(results, previousPage = previousPage, nextPage = nextPage)
    PageResult.empty
  }

  case class Criteria(expression: String, params: List[Any])

}