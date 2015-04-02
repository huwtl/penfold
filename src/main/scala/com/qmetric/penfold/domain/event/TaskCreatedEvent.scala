package com.qmetric.penfold.domain.event

import com.qmetric.penfold.domain.model._
import org.joda.time.DateTime
import com.qmetric.penfold.domain.model.AggregateId
import com.qmetric.penfold.domain.model.Payload

trait TaskCreatedEvent extends TaskEvent {
  val aggregateId: AggregateId
  val aggregateVersion: AggregateVersion
  val created: DateTime
  val queue: QueueId
  val triggerDate: DateTime
  val payload: Payload
  val score: Long
}