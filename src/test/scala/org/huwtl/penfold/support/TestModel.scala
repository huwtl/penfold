package org.huwtl.penfold.support

import org.joda.time.DateTime
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.domain.model.Status.{Waiting, Started, Ready, Closed}
import org.huwtl.penfold.command._
import org.huwtl.penfold.domain.event.TaskUnassigned
import org.huwtl.penfold.domain.event.TaskRequeued
import org.huwtl.penfold.command.UpdateTaskPayload
import org.huwtl.penfold.command.UnassignTask
import org.huwtl.penfold.domain.event.TaskRescheduled
import org.huwtl.penfold.domain.event.TaskPayloadUpdated
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.event.FutureTaskCreated
import org.huwtl.penfold.domain.event.TaskArchived
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.readstore.TaskProjection
import org.huwtl.penfold.domain.event.TaskTriggered
import scala.Some
import org.huwtl.penfold.domain.event.TaskCreated
import org.huwtl.penfold.domain.event.TaskStarted
import org.huwtl.penfold.domain.event.TaskClosed
import org.huwtl.penfold.domain.model.User
import org.huwtl.penfold.readstore.PreviousStatus
import org.huwtl.penfold.command.CreateTask
import org.huwtl.penfold.domain.model.patch.Patch
import org.huwtl.penfold.command.CreateFutureTask
import org.huwtl.penfold.domain.model.QueueBinding

object TestModel {
  val createdDate = new DateTime(2014, 2, 25, 13, 0, 0, 0)

  val triggerDate = new DateTime(2014, 2, 25, 14, 0, 0, 0)

  val score = triggerDate.getMillis

  val aggregateId = AggregateId("1")

  val version = AggregateVersion.init

  val queueId = QueueId("abc")

  val assignee = User("user1")

  val concluder = User("user1")

  val updateType = "updateType"

  val unassignType = "unassignType"

  val rescheduleReason = "schType"

  val requeueType = "reqType"

  val closeReason = "type"

  val emptyPayload = Payload.empty

  val payload = Payload(Map("data" -> "value", "inner" -> Map("bool" -> true)))

  val complexPayload = Payload(Map("data" -> "value", "inner" -> Map("bool" -> true, "inner2" -> List(Map("a" -> "1", "b" -> 1), Map("a" -> "2", "b" -> 2)))))

  val previousStatus = PreviousStatus(Started, new DateTime(2014, 2, 25, 13, 0, 0, 0))

  val payloadUpdate = Patch(List())

  object commands {
    val createTask = CreateTask(QueueBinding(TestModel.queueId), payload, Some(score))

    val createFutureTask = CreateFutureTask(QueueBinding(TestModel.queueId), triggerDate, payload, Some(score))

    val unassignTask = UnassignTask(aggregateId, version, Some(unassignType), Some(payloadUpdate))

    val updateTaskPayload = UpdateTaskPayload(aggregateId, version, Some(updateType), payloadUpdate, Some(score))

    val startTask = StartTask(aggregateId, version, Some(assignee), Some(payloadUpdate))

    val requeueTask = RequeueTask(aggregateId, version, Some(requeueType), Some(assignee), Some(payloadUpdate), Some(score))

    val rescheduleTask = RescheduleTask(aggregateId, version, triggerDate, Some(assignee), Some(rescheduleReason), Some(payloadUpdate), Some(score))

    val closeTask = CloseTask(aggregateId, version, Some(concluder), Some(closeReason), Some(assignee), Some(payloadUpdate))
  }

  object readModels {
    val task = TaskProjection(aggregateId, AggregateVersion.init, createdDate, QueueBinding(queueId), Status.Ready, createdDate, None, None, triggerDate, score, score, payload)

    val readyTask = task

    val triggeredTask = task.copy(previousStatus = Some(PreviousStatus(Waiting, createdDate)))

    val waitingTask = task.copy(status = Waiting)

    val startedTask = task.copy(version = AggregateVersion(2), status = Started, assignee = Some(assignee), previousStatus = Some(PreviousStatus(Ready, createdDate)), sort = createdDate.getMillis)

    val closedTask = startedTask.copy(version = AggregateVersion(3), status = Closed, previousStatus = Some(previousStatus), assignee = Some(concluder), closeReason = Some(closeReason))

    val requeuedTask = task.copy(version = AggregateVersion(2), previousStatus = Some(previousStatus))

    val rescheduledTask = task.copy(version = AggregateVersion(3), status = Waiting, previousStatus = Some(previousStatus), assignee = Some(assignee), sort = score, rescheduleReason = Some(rescheduleReason))
  }

  object events {
    val createdEvent = TaskCreated(aggregateId, AggregateVersion(1), createdDate, QueueBinding(queueId), triggerDate, payload, score)

    val triggeredEvent = TaskTriggered(aggregateId, AggregateVersion(2), createdDate)

    val futureCreatedEvent = FutureTaskCreated(aggregateId, AggregateVersion(1), createdDate, QueueBinding(queueId), triggerDate, payload, score)

    val startedEvent = TaskStarted(aggregateId, AggregateVersion(2), createdDate, Some(assignee), Some(payloadUpdate))

    val closedEvent = TaskClosed(aggregateId, AggregateVersion(3), createdDate, Some(concluder), Some(closeReason), Some(assignee), Some(payloadUpdate))

    val requeuedEvent = TaskRequeued(aggregateId, AggregateVersion(4), createdDate, Some(requeueType), Some(assignee), Some(payloadUpdate), Some(score))

    val rescheduledEvent = TaskRescheduled(aggregateId, AggregateVersion(3), createdDate, triggerDate, Some(assignee), Some(rescheduleReason), Some(payloadUpdate), Some(score))

    val unassignedEvent = TaskUnassigned(aggregateId, AggregateVersion(3), createdDate, Some(unassignType), Some(payloadUpdate))

    val payloadUpdatedEvent = TaskPayloadUpdated(aggregateId, AggregateVersion(2), createdDate, payloadUpdate, Some(updateType), Some(score))

    val archivedEvent = TaskArchived(aggregateId, createdEvent.aggregateVersion.next, createdDate)
  }

}
