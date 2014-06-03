package com.qmetric.penfold.app.readstore.mongodb

import com.qmetric.penfold.domain.model.AggregateId

case class LastKnownPageDetails(id: AggregateId, sortValue: Long, direction: NavigationDirection)

sealed trait NavigationDirection

object NavigationDirection {
  case object Reverse extends NavigationDirection

  case object Forward extends NavigationDirection
}
