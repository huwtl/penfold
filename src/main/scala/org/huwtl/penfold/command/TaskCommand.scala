package org.huwtl.penfold.command

import org.joda.time.DateTime
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.patch.Patch

sealed trait TaskCommand extends Command

case class CreateTask(queue: QueueId,
                      payload: Payload,
                      score: Option[Long]) extends TaskCommand

case class CreateFutureTask(queue: QueueId,
                            triggerDate: DateTime,
                            payload: Payload,
                            score: Option[Long]) extends TaskCommand

case class TriggerTask(id: AggregateId, version: AggregateVersion) extends TaskCommand

case class StartTask(id: AggregateId,
                     version: AggregateVersion,
                     assignee: Option[User],
                     payloadUpdate: Option[Patch]) extends TaskCommand

case class RequeueTask(id: AggregateId,
                       version: AggregateVersion,
                       reason: Option[String],
                       assignee: Option[User],
                       payloadUpdate: Option[Patch],
                       scoreUpdate: Option[Long]) extends TaskCommand

case class RescheduleTask(id: AggregateId,
                          version: AggregateVersion,
                          triggerDate: DateTime,
                          assignee: Option[User],
                          reason: Option[String],
                          payloadUpdate: Option[Patch],
                          scoreUpdate: Option[Long]) extends TaskCommand

case class CloseTask(id: AggregateId,
                     version: AggregateVersion,
                     user: Option[User],
                     reason: Option[String],
                     payloadUpdate: Option[Patch]) extends TaskCommand

case class CancelTask(id: AggregateId,
                     version: AggregateVersion,
                     user: Option[User],
                     reason: Option[String],
                     payloadUpdate: Option[Patch]) extends TaskCommand

case class ArchiveTask(id: AggregateId, version: AggregateVersion) extends TaskCommand

case class UpdateTaskPayload(id: AggregateId,
                             version: AggregateVersion,
                             updateType: Option[String],
                             payloadUpdate: Patch,
                             scoreUpdate: Option[Long]) extends TaskCommand

case class UnassignTask(id: AggregateId,
                        version: AggregateVersion,
                        reason: Option[String],
                        payloadUpdate: Option[Patch]) extends TaskCommand
