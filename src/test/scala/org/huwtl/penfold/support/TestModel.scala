package org.huwtl.penfold.support

import org.huwtl.penfold.command.{CreateFutureTask, CreateTask, UnassignTask, UpdateTaskPayload, _}
import org.huwtl.penfold.domain.event._
import org.huwtl.penfold.domain.model.Status._
import org.huwtl.penfold.domain.model.{AggregateId, QueueId, User, _}
import org.huwtl.penfold.domain.model.patch.Patch
import org.huwtl.penfold.readstore.{PreviousStatus, TaskProjection}
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.CloseResultType.Success

object TestModel {
  val createdDate = new DateTime(2014, 2, 25, 13, 0, 0, 0)

  val triggerDate = new DateTime(2014, 2, 25, 14, 0, 0, 0)

  val score = triggerDate.getMillis

  val aggregateId = AggregateId("1")

  val version = AggregateVersion.init

  val queueId = QueueId("abc")

  val assignee = User("user1")

  val user = User("user1")

  val updateType = "updateType"

  val unassignReason = "unassignType"

  val rescheduleReason = "schType"

  val requeueReason = "reqType"

  val closeReason = "type"

  val emptyPayload = Payload.empty

  val payload = Payload(Map("data" -> "value", "inner" -> Map("bool" -> true)))

  val complexPayload = Payload(Map("data" -> "value", "inner" -> Map("bool" -> true, "inner2" -> List(Map("a" -> "1", "b" -> 1), Map("a" -> "2", "b" -> 2)))))

  val previousStatus = PreviousStatus(Started, new DateTime(2014, 2, 25, 13, 0, 0, 0))

  val payloadUpdate = Patch(List())

  object commands {
    val createTask = CreateTask(TestModel.queueId, payload, Some(score))

    val createFutureTask = CreateFutureTask(TestModel.queueId, triggerDate, payload, Some(score))

    val unassignTask = UnassignTask(aggregateId, version, Some(unassignReason), Some(payloadUpdate))

    val updateTaskPayload = UpdateTaskPayload(aggregateId, version, Some(updateType), payloadUpdate, Some(score))

    val startTask = StartTask(aggregateId, version, Some(assignee), Some(payloadUpdate))

    val requeueTask = RequeueTask(aggregateId, version, Some(requeueReason), Some(assignee), Some(payloadUpdate), Some(score))

    val rescheduleTask = RescheduleTask(aggregateId, version, triggerDate, Some(assignee), Some(rescheduleReason), Some(payloadUpdate), Some(score))

    val closeTask = CloseTask(aggregateId, version, Some(user), Some(closeReason), Some(Success), Some(payloadUpdate))

    val cancelTask = CancelTask(aggregateId, version, Some(user), Some(closeReason), Some(payloadUpdate))
  }

  object readModels {
    val task = TaskProjection(aggregateId, AggregateVersion.init, createdDate, queueId, Status.Ready, createdDate, None, 0, None, triggerDate, score, score, payload)

    val readyTask = task

    val triggeredTask = task.copy(previousStatus = Some(PreviousStatus(Waiting, createdDate)))

    val waitingTask = task.copy(status = Waiting)

    val startedTask = task.copy(version = AggregateVersion(2), status = Started, attempts = 1, assignee = Some(assignee), previousStatus = Some(PreviousStatus(Ready, createdDate)), sort = createdDate.getMillis)

    val closedTask = startedTask.copy(version = AggregateVersion(3), status = Closed, previousStatus = Some(previousStatus), assignee = None, closeReason = Some(closeReason), closeResultType = Some(Success))

    val cancelledTask = startedTask.copy(version = AggregateVersion(3), status = Cancelled, previousStatus = Some(previousStatus), assignee = None, cancelReason = Some(closeReason))

    val requeuedTask = task.copy(version = AggregateVersion(2), previousStatus = Some(previousStatus))

    val rescheduledTask = task.copy(version = AggregateVersion(3), status = Waiting, previousStatus = Some(previousStatus), assignee = Some(assignee), sort = score, rescheduleReason = Some(rescheduleReason))
  }

  object events {
    val createdEvent = TaskCreated(aggregateId, AggregateVersion(1), createdDate, queueId, triggerDate, payload, score)

    val triggeredEvent = TaskTriggered(aggregateId, AggregateVersion(2), createdDate)

    val futureCreatedEvent = FutureTaskCreated(aggregateId, AggregateVersion(1), createdDate, queueId, triggerDate, payload, score)

    val startedEvent = TaskStarted(aggregateId, AggregateVersion(2), createdDate, Some(assignee), Some(payloadUpdate))

    val closedEvent = TaskClosed(aggregateId, AggregateVersion(3), createdDate, Some(user), Some(closeReason), Some(Success), Some(payloadUpdate))

    val cancelEvent = TaskCancelled(aggregateId, AggregateVersion(3), createdDate, Some(user), Some(closeReason), Some(payloadUpdate))

    val requeuedEvent = TaskRequeued(aggregateId, AggregateVersion(4), createdDate, Some(requeueReason), Some(assignee), Some(payloadUpdate), Some(score))

    val rescheduledEvent = TaskRescheduled(aggregateId, AggregateVersion(3), createdDate, triggerDate, Some(assignee), Some(rescheduleReason), Some(payloadUpdate), Some(score))

    val unassignedEvent = TaskUnassigned(aggregateId, AggregateVersion(3), createdDate, Some(unassignReason), Some(payloadUpdate))

    val payloadUpdatedEvent = TaskPayloadUpdated(aggregateId, AggregateVersion(2), createdDate, payloadUpdate, Some(updateType), Some(score))

    val archivedEvent = TaskArchived(aggregateId, createdEvent.aggregateVersion.next, createdDate)
  }

}
