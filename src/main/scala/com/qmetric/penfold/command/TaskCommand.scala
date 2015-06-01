package com.qmetric.penfold.command

import org.joda.time.DateTime
import com.qmetric.penfold.domain.model._
import com.qmetric.penfold.domain.model.AggregateId
import com.qmetric.penfold.domain.model.patch.Patch

sealed trait TaskCommand extends Command

case class CreateTask(queueBinding: QueueBinding,
                      payload: Payload,
                      score: Option[Long]) extends TaskCommand

case class CreateFutureTask(queueBinding: QueueBinding,
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
                       requeueType: Option[String],
                       assignee: Option[User],
                       payloadUpdate: Option[Patch],
                       scoreUpdate: Option[Long]) extends TaskCommand

case class RescheduleTask(id: AggregateId,
                          version: AggregateVersion,
                          triggerDate: DateTime,
                          assignee: Option[User],
                          rescheduleType: Option[String],
                          payloadUpdate: Option[Patch],
                          scoreUpdate: Option[Long]) extends TaskCommand

case class CloseTask(id: AggregateId,
                     version: AggregateVersion,
                     concluder: Option[User],
                     conclusionType: Option[String],
                     assignee: Option[User],
                     payloadUpdate: Option[Patch]) extends TaskCommand

case class ArchiveTask(id: AggregateId, version: AggregateVersion) extends TaskCommand

case class UpdateTaskPayload(id: AggregateId,
                             version: AggregateVersion,
                             updateType: Option[String],
                             payloadUpdate: Patch,
                             scoreUpdate: Option[Long]) extends TaskCommand

case class UnassignTask(id: AggregateId,
                        version: AggregateVersion,
                        unassignType: Option[String],
                        payloadUpdate: Option[Patch]) extends TaskCommand

case class ReassignTask(id: AggregateId,
                        version: AggregateVersion,
                        assignee: User,
                        reassignType: Option[String],
                        payloadUpdate: Option[Patch]) extends TaskCommand
