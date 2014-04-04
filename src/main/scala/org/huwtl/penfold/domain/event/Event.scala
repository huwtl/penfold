package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{AggregateType, AggregateVersion, AggregateId}
import org.joda.time.DateTime

trait Event {
  val aggregateId: AggregateId
  val aggregateVersion: AggregateVersion
  val aggregateType: AggregateType
  val created: DateTime
}









