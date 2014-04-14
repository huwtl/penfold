package org.huwtl.penfold.app.support.hal

import org.huwtl.penfold.readstore.{PageRequest, NavigationDirection, PageResult, Filters}
import com.theoryinpractise.halbuilder.api.RepresentationFactory
import org.scalatra.util.RicherString
import org.huwtl.penfold.readstore.NavigationDirection.{Reverse, Forward}
import org.huwtl.penfold.domain.model.AggregateId

trait PaginatedRepresentationProvider {

  def getRepresentation(pageRequest: PageRequest, pageResult: PageResult, filters: Filters, baseSelfLink: String, representationFactory: RepresentationFactory) = {
    val filterParams = filterParameters(filters)

    val queryString = s"${paramQueryString(if (pageResult.previousExists) selfPageParams(pageRequest) :: filterParams else filterParams)}"

    val root = representationFactory.newRepresentation(s"$baseSelfLink${queryString}")

    if (pageResult.previousExists) {
      val pageRef = pageReference(pageResult.entries.head.id, pageResult.entries.head.triggerDate.getMillis, Reverse)
      val pageParameter = pageParam(pageRef)
      root.withLink("previous", s"${baseSelfLink}${paramQueryString(pageParameter :: filterParams)}", pageRef, null, null, null)
    }

    if (pageResult.nextExists) {
      val pageRef = pageReference(pageResult.entries.last.id, pageResult.entries.last.triggerDate.getMillis, Forward)
      val pageParameter = pageParam(pageRef)
      root.withLink("next", s"${baseSelfLink}${paramQueryString(pageParameter :: filterParams)}", pageRef, null, null, null)
    }

    root
  }

  private def selfPageParams(pageRequest: PageRequest) = {
    val lastKnownPageDetails = pageRequest.lastKnownPageDetails.get
    pageParam(pageReference(lastKnownPageDetails.id, lastKnownPageDetails.score, lastKnownPageDetails.direction))
  }

  private def pageReference(lastId: AggregateId, lastScore: Long, direction: NavigationDirection) = {
    val directionValue = if (direction == Forward) 1 else 0
    s"${lastId.value}~${lastScore}~${directionValue}"
  }

  private def pageParam(pageRef: String) = {
    s"pageRef=${pageRef}"
  }

  private def paramQueryString(paramStrings: List[String]) = paramStrings match {
    case Nil => ""
    case params => params.mkString("?", "&", "")
  }

  private def filterParameters(filters: Filters) = filters.all.map(filter => s"_${encode(filter.key)}=${encode(filter.value)}").toList

  private def encode(str: String) = new RicherString(str).urlEncode.replace("&", "%26")
}
