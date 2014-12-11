package org.huwtl.penfold.app.support.hal

import org.huwtl.penfold.readstore._
import com.theoryinpractise.halbuilder.api.RepresentationFactory
import org.scalatra.util.RicherString
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.readstore.PageRequest
import org.huwtl.penfold.readstore.PageReference

trait PaginatedRepresentationProvider {

  val objectSerializer = new ObjectSerializer

  def getRepresentation(pageRequest: PageRequest, pageResult: PageResult, filters: Filters, baseSelfLink: String, representationFactory: RepresentationFactory) = {
    val filterParam = filterParameter(filters)

    val queryString = s"${paramQueryString(if (pageResult.previousExists) selfPageParams(pageRequest) :: filterParam.toList else filterParam.toList)}"

    val root = representationFactory.newRepresentation(s"$baseSelfLink${queryString}")

    if (pageResult.previousExists) {
      val pageRef = pageResult.previousPage.get
      val pageParameter = pageParam(pageRef)
      root.withLink("previous", s"${baseSelfLink}${paramQueryString(pageParameter :: filterParam.toList)}", pageRef.value, null, null, null)
    }

    if (pageResult.nextExists) {
      val pageRef = pageResult.nextPage.get
      val pageParameter = pageParam(pageRef)
      root.withLink("next", s"${baseSelfLink}${paramQueryString(pageParameter :: filterParam.toList)}", pageRef.value, null, null, null)
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

  private def filterParameter(filters: Filters) = {
    filters match {
      case Filters(Nil) => None
      case Filters(all) => Some(s"q=${encode(objectSerializer.serialize[List[Filter]](all))}")
    }
  }

  private def encode(str: String) = new RicherString(str).urlEncode.replace("&", "%26").replace("[", "%5B").replace("]", "%5D").replace(",", "%2C").replace(":", "%3A")
}
