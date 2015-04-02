package com.qmetric.penfold.app.readstore.postgres

import com.qmetric.penfold.readstore.{TaskProjection, PageReference}
import com.qmetric.penfold.domain.model.AggregateId
import com.qmetric.penfold.app.readstore.postgres.NavigationDirection.{Reverse, Forward}

class LastKnownPageDetailsTransformer {
  private val pageReferenceSeparator = "~"

  def toPageDetails(pageReference: Option[PageReference]) = {
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

  def toPageReference(results: List[TaskProjection], direction: NavigationDirection) = {
    direction match {
      case Forward => Some(PageReference(Array(results.last.id.value, results.last.sort, 1) mkString pageReferenceSeparator))
      case Reverse => Some(PageReference(Array(results.head.id.value, results.head.sort, 0) mkString pageReferenceSeparator))
    }
  }
}
