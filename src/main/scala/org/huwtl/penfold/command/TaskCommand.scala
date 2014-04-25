package org.huwtl.penfold.command

import org.joda.time.DateTime
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.patch.Patch

sealed trait TaskCommand extends Command

case class CreateTask(queueBinding: QueueBinding,
                      payload: Payload,
                      score: Option[Long]) extends TaskCommand

case class CreateFutureTask(queueBinding: QueueBinding,
                            triggerDate: DateTime,
                            payload: Payload,
                            score: Option[Long]) extends TaskCommand

case class TriggerTask(id: AggregateId) extends TaskCommand

case class StartTask(id: AggregateId) extends TaskCommand

case class CompleteTask(id: AggregateId) extends TaskCommand

case class CancelTask(id: AggregateId) extends TaskCommand

case class UpdateTaskPayload(id: AggregateId,
                             version: AggregateVersion,
                             updateType: Option[String],
                             payloadUpdate: Patch,
                             score: Option[Long]) extends TaskCommand
