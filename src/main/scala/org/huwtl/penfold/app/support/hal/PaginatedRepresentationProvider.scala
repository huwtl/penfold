package org.huwtl.penfold.app.support.hal

import org.huwtl.penfold.query.{PageResult, Filters}
import com.theoryinpractise.halbuilder.api.RepresentationFactory
import org.scalatra.util.RicherString

trait PaginatedRepresentationProvider {

  def getRepresentation(pageOfJobs: PageResult, filters: Filters, baseSelfLink: String, representationFactory: RepresentationFactory) = {
    val filterParams = filterParameters(filters)

    val queryString = s"${paramQueryString(if (pageOfJobs.previousExists) pageParam(pageOfJobs.pageNumber) :: filterParams else filterParams)}"

    val root = representationFactory.newRepresentation(s"$baseSelfLink${queryString}")

    if (pageOfJobs.previousExists) {
      root.withLink("previous", s"${baseSelfLink}${paramQueryString(pageParam(pageOfJobs.previousPageNumber) :: filterParams)}")
    }

    if (pageOfJobs.nextExists) {
      root.withLink("next", s"${baseSelfLink}${paramQueryString(pageParam(pageOfJobs.nextPageNumber) :: filterParams)}")
    }

    root
  }

  private def pageParam(pageNumber: Int) = s"page=${pageNumber}"

  private def paramQueryString(paramStrings: List[String]) = paramStrings match {
    case Nil => ""
    case params => params.mkString("?", "&", "")
  }

  private def filterParameters(filters: Filters) = filters.all.map(filter => s"_${encode(filter.key)}=${encode(filter.value)}").toList

  private def encode(str: String) = new RicherString(str).urlEncode.replace("&", "%26")
}
