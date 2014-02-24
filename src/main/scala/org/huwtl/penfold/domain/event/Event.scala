package org.huwtl.penfold.domain.event

import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.{Version, Payload, QueueName, Id}

sealed trait Event {
  val aggregateId: Id
  val aggregateVersion: Version
}

case class JobCreated(aggregateId: Id,
                      aggregateVersion: Version,
                      queueName: QueueName,
                      created: DateTime,
                      triggerDate: DateTime,
                      payload: Payload) extends Event

case class JobTriggered(aggregateId: Id, aggregateVersion: Version) extends Event

case class JobStarted(aggregateId: Id, aggregateVersion: Version) extends Event

case class JobCompleted(aggregateId: Id, aggregateVersion: Version) extends Event

case class JobCancelled(aggregateId: Id, aggregateVersion: Version) extends Event