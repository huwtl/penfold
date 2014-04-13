package org.huwtl.penfold.readstore

import org.huwtl.penfold.domain.model.AggregateId

case class LastKnownPageDetails(id: AggregateId, score: Long, direction: NavigationDirection)

sealed trait NavigationDirection

object NavigationDirection {
  case object Reverse extends NavigationDirection

  case object Forward extends NavigationDirection
}
