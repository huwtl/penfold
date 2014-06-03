package com.qmetric.penfold.domain.event

import com.qmetric.penfold.domain.model.{AggregateType, AggregateVersion, AggregateId}
import org.joda.time.DateTime

trait Event {
  val aggregateId: AggregateId
  val aggregateVersion: AggregateVersion
  val aggregateType: AggregateType
  val created: DateTime
}









