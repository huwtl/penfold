package org.huwtl.penfold.app.web

import org.scalatra.Params
import org.huwtl.penfold.readstore.{LastKnownPageDetails, PageRequest}
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.readstore.NavigationDirection.{Reverse, Forward}

trait PageRequestProvider {
  def parsePageRequestParams(params: Params, pageSize: Int) = {
    val idFromLastViewedPage = params.get("lastId")
    val scoreFromLastViewedPage = params.get("lastScore")
    val navigationalDirection = params.get("direction")

    if (scoreFromLastViewedPage.isDefined && scoreFromLastViewedPage.isDefined && navigationalDirection.isDefined) {
      PageRequest(pageSize, Some(LastKnownPageDetails(
        AggregateId(idFromLastViewedPage.get),
        scoreFromLastViewedPage.get.toLong,
        if (navigationalDirection.get == "1") Forward else Reverse)))
    }
    else {
      PageRequest(pageSize)
    }
  }
}
