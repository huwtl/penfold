package org.huwtl.penfold.command

import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.{Binding, Payload, QueueId, AggregateId}

sealed trait JobCommand extends Command

case class CreateJob(binding: Binding,
                     payload: Payload) extends JobCommand


case class CreateFutureJob(binding: Binding,
                           triggerDate: DateTime,
                           payload: Payload) extends JobCommand

case class TriggerJob(id: AggregateId) extends JobCommand

case class StartJob(id: AggregateId, queueId: QueueId) extends JobCommand

case class CompleteJob(id: AggregateId, queueId: QueueId) extends JobCommand

case class CancelJob(id: AggregateId, queueId: QueueId) extends JobCommand
