package com.qmetric.penfold.app.support.hal

import com.qmetric.penfold.readstore.{PageReference, PageRequest, PageResult, Filters}
import com.theoryinpractise.halbuilder.api.RepresentationFactory
import org.scalatra.util.RicherString
import scala.collection.SortedSet

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

  private def filterParameters(filters: Filters) = {
    for {
      filter <- filters.all
      filterValue <- SortedSet(filter.values.toList :_*)
    } yield s"_${encode(filter.key)}=${encode(filterValue getOrElse "")}"
  }

  private def encode(str: String) = new RicherString(str).urlEncode.replace("&", "%26")
}
