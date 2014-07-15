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

case class TriggerTask(id: AggregateId) extends TaskCommand

case class StartTask(id: AggregateId,
                     assignee: Option[Assignee]) extends TaskCommand

case class RequeueTask(id: AggregateId) extends TaskCommand

case class RescheduleTask(id: AggregateId,
                          triggerDate: DateTime,
                          assignee: Option[Assignee],
                          rescheduleType: Option[String]) extends TaskCommand

case class CloseTask(id: AggregateId,
                     concluder: Option[Assignee] = None,
                     conclusionType: Option[String] = None) extends TaskCommand

case class ArchiveTask(id: AggregateId) extends TaskCommand

case class UpdateTaskPayload(id: AggregateId,
                             version: AggregateVersion,
                             updateType: Option[String],
                             payloadUpdate: Patch,
                             score: Option[Long]) extends TaskCommand
