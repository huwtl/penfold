package org.huwtl.penfold.command

import org.joda.time.DateTime
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.QueueId

sealed trait TaskCommand extends Command

case class CreateTask(queueBinding: QueueBinding,
                     payload: Payload) extends TaskCommand


case class CreateFutureTask(queueBinding: QueueBinding,
                           triggerDate: DateTime,
                           payload: Payload) extends TaskCommand

case class TriggerTask(id: AggregateId) extends TaskCommand

case class StartTask(id: AggregateId, queueId: QueueId) extends TaskCommand

case class CompleteTask(id: AggregateId, queueId: QueueId) extends TaskCommand

case class CancelTask(id: AggregateId, queueId: QueueId) extends TaskCommand
