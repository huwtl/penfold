package org.huwtl.penfold.domain.event

import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.{Version, Payload, QueueName, AggregateId}

sealed trait Event {
  val aggregateId: AggregateId
  val aggregateVersion: Version
}

case class JobCreated(aggregateId: AggregateId,
                      aggregateVersion: Version,
                      queueName: QueueName,
                      created: DateTime,
                      triggerDate: DateTime,
                      payload: Payload) extends Event

case class JobTriggered(aggregateId: AggregateId, aggregateVersion: Version) extends Event

case class JobStarted(aggregateId: AggregateId, aggregateVersion: Version) extends Event

case class JobCompleted(aggregateId: AggregateId, aggregateVersion: Version) extends Event

case class JobCancelled(aggregateId: AggregateId, aggregateVersion: Version) extends Event