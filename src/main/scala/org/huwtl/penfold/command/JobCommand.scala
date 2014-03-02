package org.huwtl.penfold.command

import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.{Payload, QueueName, AggregateId}

sealed trait JobCommand extends Command

case class CreateJob(id: AggregateId,
                     queueName: QueueName,
                     payload: Payload) extends JobCommand


case class CreateFutureJob(id: AggregateId,
                           queueName: QueueName,
                           triggerDate: DateTime,
                           payload: Payload) extends JobCommand

case class TriggerJob(id: AggregateId) extends JobCommand

case class StartJob(id: AggregateId) extends JobCommand

case class CompleteJob(id: AggregateId) extends JobCommand

case class CancelJob(id: AggregateId) extends JobCommand
