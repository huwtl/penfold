package org.huwtl.penfold.app.web

import org.scalatra.Params
import org.huwtl.penfold.readstore.{LastKnownPageDetails, PageRequest}
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.readstore.NavigationDirection.{Reverse, Forward}

trait PageRequestProvider {
  def parsePageRequestParams(params: Params, pageSize: Int) = {
    val pageReference = params.get("pageRef")

    if (pageReference.isDefined) {
      pageReference.get.split('~') match {
        case Array(idFromLastViewedPage, scoreFromLastViewedPage, navigationalDirection) => {
          PageRequest(pageSize, Some(LastKnownPageDetails(
            AggregateId(idFromLastViewedPage),
            scoreFromLastViewedPage.toLong,
            if (navigationalDirection == "1") Forward else Reverse))
          )
        }
        case _ => PageRequest(pageSize)
      }
    }
    else {
      PageRequest(pageSize)
    }
  }
}
