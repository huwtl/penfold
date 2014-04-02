package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{Version, AggregateId}
import org.joda.time.DateTime

trait Event {
  val aggregateId: AggregateId
  val aggregateVersion: Version
  val created: DateTime
}









