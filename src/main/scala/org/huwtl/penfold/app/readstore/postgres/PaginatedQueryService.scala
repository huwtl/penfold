package org.huwtl.penfold.app.readstore.postgres

import org.huwtl.penfold.app.readstore.postgres.NavigationDirection.{Forward, Reverse}
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.readstore.{PageRequest, TaskProjection, _}

import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import grizzled.slf4j.Logger
import org.huwtl.penfold.app.readstore.postgres.MyPostgresDriver.simple._
import org.huwtl.penfold.app.readstore.postgres.TasksTable.TasksTable
import scala.slick.jdbc.{StaticQuery => Q}
import scala.slick.lifted

class PaginatedQueryService(database: Database, objectSerializer: ObjectSerializer, aliases: Aliases) {

  private lazy val logger = Logger(getClass)

  val sortColumn = "sort"

  val lastKnownPageTransformer = new LastKnownPageDetailsTransformer()

  def execQuery(filters: Filters, pageRequest: PageRequest, sortOrder: SortOrder): PageResult = {
    val pageSize = pageRequest.pageSize

    val query = TasksTable.Tasks

    lastKnownPageTransformer.toPageDetails(pageRequest.pageReference) match {
      case Some(lastKnownPageDetails) =>
        (lastKnownPageDetails.direction, sortOrder) match {
          case (Forward, SortOrder.Desc) => queryForward(query, filters, pageSize, lastKnownPageDetails, SortOrder.Desc, movingToLesserSortScores = true)
          case (Forward, SortOrder.Asc) => queryForward(query, filters, pageSize, lastKnownPageDetails, SortOrder.Asc, movingToLesserSortScores = false)
          case (Reverse, SortOrder.Desc) => queryBackwards(query, filters, pageSize, lastKnownPageDetails, SortOrder.Desc, movingToLesserSortScores = false)
          case (Reverse, SortOrder.Asc) => queryBackwards(query, filters, pageSize, lastKnownPageDetails, SortOrder.Asc, movingToLesserSortScores = true)
        }
      case None =>
        val resultsWithOverflow = execPageQueryWithOverflow(query, filters, sortOrder, pageSize)
        val results = resultsWithOverflow take pageSize
        val nextPage = if (resultsWithOverflow.size > pageSize) lastKnownPageTransformer.toPageReference(results, Forward) else None

        PageResult(results, previousPage = None, nextPage = nextPage)
    }
  }

  private def execPageQueryWithOverflow(query: lifted.Query[TasksTable, TasksTable#TableElementType, Seq], filters: Filters, sortOrder: SortOrder, pageSize: Int) = {
    if (pageSize > 0) {
      val queryWithCriteria: lifted.Query[TasksTable, TasksTable#TableElementType, Seq] = filters.all.foldLeft(query)((q, f) => {

        val filterPath = aliases.path(Alias(f.key)).value

        q.filter((row: TasksTable) =>
          f match {
            case EQ(key, value, dataType) => jsonPath(row, filterPath) === value.bind
            case IN(key, values, dataType) => jsonPath(row, filterPath) inSetBind values
            case LT(key, value, dataType) => jsonPath(row, filterPath).asColumnOf[Long] < Option(value).map(_.toLong).getOrElse(Long.MinValue).bind
            case GT(key, value, dataType) => jsonPath(row, filterPath).asColumnOf[Long] > Option(value).map(_.toLong).getOrElse(Long.MaxValue).bind
            case _ => throw new IllegalStateException("unsupported filter type")
          })
      })

      val sort = sortCriteria(queryWithCriteria, sortOrder).take(pageSize + 1)

      val rows = sort.map(_.data)

      logger.info(s"query: ${rows.selectStatement}")

      rows.list.map(row => objectSerializer.deserialize[TaskData](row.value).toTaskProjection)
    }
    else {
      List.empty
    }
  }

  private def jsonPath(row: TasksTable, path: String) = {
    val parts = path.split("\\.").toList

    parts match {
      case List(part) => row.data.+>>(part)
      case firstPart :: otherParts => otherParts.init.foldLeft(row.data.+>(firstPart))((currentJsonPath, nextPart) => {
        currentJsonPath.+>(nextPart)
      }).+>>(parts.last)
    }
  }

  private def enforcePageSortOrder(results: List[TaskProjection], sortOrder: SortOrder) = {
    sortOrder match {
      case SortOrder.Desc => results.sortWith((e1, e2) => e1.id.value > e2.id.value).sortWith((e1, e2) => e1.sort > e2.sort)
      case SortOrder.Asc => results.sortWith((e1, e2) => e1.id.value < e2.id.value).sortWith((e1, e2) => e1.sort < e2.sort)
    }
  }

  private def sortOrderForNavigationDirection(movingToLesserSortScores: Boolean) = {
    if (movingToLesserSortScores) SortOrder.Desc else SortOrder.Asc
  }

  private def sortCriteria(query: lifted.Query[TasksTable, TasksTable#TableElementType, Seq], sortOrder: SortOrder) = {
    if (sortOrder == SortOrder.Desc)
      query.sortBy(_.id.desc).sortBy(row => row.data.+>>(sortColumn).asColumnOf[Long].desc)
    else
      query.sortBy(_.id.asc).sortBy(row => row.data.+>>(sortColumn).asColumnOf[Long].asc)
  }

  private def pagePositionRestriction(query: lifted.Query[TasksTable, TasksTable#TableElementType, Seq], lastKnownDetails: LastKnownPageDetails, movingToLesserSortScores: Boolean) = {
    if (movingToLesserSortScores) {
      query.filter(row => {
        (row.data.+>>(sortColumn).asColumnOf[Long] === lastKnownDetails.sortValue.bind && row.id < lastKnownDetails.id.value.bind) || row.data.+>>(sortColumn).asColumnOf[Long] < lastKnownDetails.sortValue.bind
      })
    }
    else {
      query.filter(row => {
        (row.data.+>>(sortColumn).asColumnOf[Long] === lastKnownDetails.sortValue.bind && row.id > lastKnownDetails.id.value.bind) || row.data.+>>(sortColumn).asColumnOf[Long] > lastKnownDetails.sortValue.bind
      })
    }
  }

  private def queryForward(query: lifted.Query[TasksTable, TasksTable#TableElementType, Seq], filters: Filters, pageSize: Int, lastKnownPageDetails: LastKnownPageDetails, sortOrder: SortOrder, movingToLesserSortScores: Boolean) = {
    val skipForwardFromLastVisitedPage = pagePositionRestriction(query, lastKnownPageDetails, movingToLesserSortScores)
    val resultsWithOverflow = execPageQueryWithOverflow(skipForwardFromLastVisitedPage, filters, sortOrderForNavigationDirection(movingToLesserSortScores), pageSize)
    val results = enforcePageSortOrder(resultsWithOverflow take pageSize, sortOrder)
    val previousPage = if (results.nonEmpty) lastKnownPageTransformer.toPageReference(results, Reverse) else None
    val nextPage = if (resultsWithOverflow.size > pageSize) lastKnownPageTransformer.toPageReference(results, Forward) else None

    PageResult(results, previousPage = previousPage, nextPage = nextPage)
  }

  private def queryBackwards(query: lifted.Query[TasksTable, TasksTable#TableElementType, Seq], filters: Filters, pageSize: Int, lastKnownPageDetails: LastKnownPageDetails, sortOrder: SortOrder, movingToLesserSortScores: Boolean) = {
    val skipBackFromLastVisitedPage = pagePositionRestriction(query, lastKnownPageDetails, movingToLesserSortScores)
    val resultsWithOverflow = execPageQueryWithOverflow(skipBackFromLastVisitedPage, filters, sortOrderForNavigationDirection(movingToLesserSortScores), pageSize)
    val results = enforcePageSortOrder(resultsWithOverflow take pageSize, sortOrder)
    val previousPage = if (resultsWithOverflow.size > pageSize) lastKnownPageTransformer.toPageReference(results, Reverse) else None
    val nextPage = if (results.nonEmpty) lastKnownPageTransformer.toPageReference(results, Forward) else None

    PageResult(results, previousPage = previousPage, nextPage = nextPage)
  }
}