package org.huwtl.penfold.command

import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.{Binding, Payload, QueueId, AggregateId}

sealed trait TaskCommand extends Command

case class CreateTask(binding: Binding,
                     payload: Payload) extends TaskCommand


case class CreateFutureTask(binding: Binding,
                           triggerDate: DateTime,
                           payload: Payload) extends TaskCommand

case class TriggerTask(id: AggregateId) extends TaskCommand

case class StartTask(id: AggregateId, queueId: QueueId) extends TaskCommand

case class CompleteTask(id: AggregateId, queueId: QueueId) extends TaskCommand

case class CancelTask(id: AggregateId, queueId: QueueId) extends TaskCommand
