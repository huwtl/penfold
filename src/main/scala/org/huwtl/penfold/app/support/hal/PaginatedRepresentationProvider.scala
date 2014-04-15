package org.huwtl.penfold.app.support.hal

import org.huwtl.penfold.readstore.{PageReference, PageRequest, PageResult, Filters}
import com.theoryinpractise.halbuilder.api.RepresentationFactory
import org.scalatra.util.RicherString

trait PaginatedRepresentationProvider {

  def getRepresentation(pageRequest: PageRequest, pageResult: PageResult, filters: Filters, baseSelfLink: String, representationFactory: RepresentationFactory) = {
    val filterParams = filterParameters(filters)

    val queryString = s"${paramQueryString(if (pageResult.previousExists) selfPageParams(pageRequest) :: filterParams else filterParams)}"

    val root = representationFactory.newRepresentation(s"$baseSelfLink${queryString}")

    if (pageResult.previousExists) {
      val pageRef = pageResult.previousPage.get
      val pageParameter = pageParam(pageRef)
      root.withLink("previous", s"${baseSelfLink}${paramQueryString(pageParameter :: filterParams)}", pageRef.value, null, null, null)
    }

    if (pageResult.nextExists) {
      val pageRef = pageResult.nextPage.get
      val pageParameter = pageParam(pageRef)
      root.withLink("next", s"${baseSelfLink}${paramQueryString(pageParameter :: filterParams)}", pageRef.value, null, null, null)
    }

    root
  }

  private def selfPageParams(pageRequest: PageRequest) = {
    pageParam(pageRequest.pageReference.get)
  }

  private def pageParam(pageRef: PageReference) = {
    s"page=${pageRef.value}"
  }

  private def paramQueryString(paramStrings: List[String]) = paramStrings match {
    case Nil => ""
    case params => params.mkString("?", "&", "")
  }

  private def filterParameters(filters: Filters) = filters.all.map(filter => s"_${encode(filter.key)}=${encode(filter.value)}").toList

  private def encode(str: String) = new RicherString(str).urlEncode.replace("&", "%26")
}
