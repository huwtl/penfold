package org.huwtl.penfold.app.support.hal

import org.huwtl.penfold.readstore.{PageRequest, NavigationDirection, PageResult, Filters}
import com.theoryinpractise.halbuilder.api.RepresentationFactory
import org.scalatra.util.RicherString
import org.huwtl.penfold.readstore.NavigationDirection.{Reverse, Forward}
import org.huwtl.penfold.domain.model.AggregateId

trait PaginatedRepresentationProvider {

  def getRepresentation(pageRequest: PageRequest, pageResult: PageResult, filters: Filters, baseSelfLink: String, representationFactory: RepresentationFactory) = {
    val filterParams = filterParameters(filters)

    val queryString = s"${paramQueryString(if (pageResult.previousExists) selfPageParams(pageRequest) ::: filterParams else filterParams)}"

    val root = representationFactory.newRepresentation(s"$baseSelfLink${queryString}")

    if (pageResult.previousExists) {
      root.withLink("previous", s"${baseSelfLink}${
        paramQueryString(
          pageParams(pageResult.entries.head.id, pageResult.entries.head.triggerDate.getMillis, Reverse) ::: filterParams
        )
      }")
    }

    if (pageResult.nextExists) {
      root.withLink("next", s"${baseSelfLink}${
        paramQueryString(
          pageParams(pageResult.entries.last.id, pageResult.entries.last.triggerDate.getMillis, Forward) ::: filterParams)
      }")
    }

    root
  }

  private def selfPageParams(pageRequest: PageRequest) = {
    pageRequest.lastKnownPageDetails match {
      case Some(lastKnownPageDetails) => {
        pageParams(lastKnownPageDetails.id, lastKnownPageDetails.score, lastKnownPageDetails.direction)
      }
      case None => Nil
    }
  }

  private def pageParams(lastId: AggregateId, lastScore: Long, direction: NavigationDirection) = {
    val directionValue = if (direction == Forward) 1 else 0
    List(s"lastId=${lastId.value}", s"lastScore=${lastScore}", s"direction=${directionValue}")
  }

  private def paramQueryString(paramStrings: List[String]) = paramStrings match {
    case Nil => ""
    case params => params.mkString("?", "&", "")
  }

  private def filterParameters(filters: Filters) = filters.all.map(filter => s"_${encode(filter.key)}=${encode(filter.value)}").toList

  private def encode(str: String) = new RicherString(str).urlEncode.replace("&", "%26")
}
