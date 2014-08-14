package com.qmetric.penfold.domain.model

import org.joda.time.DateTime
import org.joda.time.DateTime.now
import com.qmetric.penfold.domain.event._
import com.qmetric.penfold.domain.event.TaskCreated
import com.qmetric.penfold.domain.model.Status._
import com.qmetric.penfold.domain.model.patch.Patch

object Task extends AggregateFactory {
  def create(aggregateId: AggregateId, queueBinding: QueueBinding, payload: Payload, score: Option[Long]) = {
    val currentDateTime = now
    val scoreValue = score getOrElse currentDateTime.getMillis
    applyTaskCreated(TaskCreated(aggregateId, AggregateVersion.init, currentDateTime, queueBinding, currentDateTime, payload, scoreValue))
  }

  def create(aggregateId: AggregateId, queueBinding: QueueBinding, triggerDate: DateTime, payload: Payload, score: Option[Long]) = {
    val scoreValue = score getOrElse triggerDate.getMillis
    val createdTask = applyFutureTaskCreated(FutureTaskCreated(aggregateId, AggregateVersion.init, now, queueBinding, triggerDate, payload, scoreValue))
    if (createdTask.triggerDate.isAfterNow) createdTask else createdTask.trigger(createdTask.version)
  }

  def applyEvent = {
    case event: TaskCreated => applyTaskCreated(event)
    case event: FutureTaskCreated => applyFutureTaskCreated(event)
    case event => unhandled(event)
  }

  private def applyTaskCreated(event: TaskCreated) = applyTaskCreatedEvent(event, Ready)

  private def applyFutureTaskCreated(event: FutureTaskCreated) = applyTaskCreatedEvent(event, Waiting)

  private def applyTaskCreatedEvent(event: TaskCreatedEvent, status: Status) = Task(
    event :: Nil,
    event.aggregateId,
    event.aggregateVersion,
    event.created,
    None,
    event.queueBinding,
    status,
    event.triggerDate,
    event.payload,
    event.score
  )
}

case class Task(uncommittedEvents: List[Event],
                aggregateId: AggregateId,
                version: AggregateVersion,
                created: DateTime,
                assignee: Option[User],
                queueBinding: QueueBinding,
                status: Status,
                triggerDate: DateTime,
                payload: Payload,
                score: Long) extends AggregateRoot {

  override def aggregateType = AggregateType.Task

  def trigger(expectedVersion: AggregateVersion): Task = {
    checkVersion(expectedVersion)
    checkConflict(status == Waiting, s"Can only trigger a waiting task ($aggregateId), but was $status")
    applyTaskTriggered(TaskTriggered(aggregateId, version.next, now))
  }

  def updatePayload(expectedVersion: AggregateVersion, payloadUpdate: Patch, updateType: Option[String], score: Option[Long]): Task = {
    checkVersion(expectedVersion)
    checkConflict(status != Closed && status != Archived, s"Cannot update payload for a closed/archived task ($aggregateId), but was $status")
    applyTaskPayloadUpdated(TaskPayloadUpdated(aggregateId, version.next, now, payloadUpdate, updateType, score))
  }

  def start(expectedVersion: AggregateVersion, assignee: Option[User], payloadUpdate: Option[Patch]): Task = {
    checkVersion(expectedVersion)
    checkConflict(status == Ready, s"Can only start a task ($aggregateId) that is ready, but was $status")
    applyTaskStarted(TaskStarted(aggregateId, version.next, now, assignee, payloadUpdate))
  }

  def close(expectedVersion: AggregateVersion, user: Option[User], completionType: Option[String], assignee: Option[User], payloadUpdate: Option[Patch]): Task = {
    checkVersion(expectedVersion)
    checkConflict(status != Closed && status != Archived, s"Cannot close an archived or already closed task ($aggregateId), but was $status")
    applyTaskClosed(TaskClosed(aggregateId, version.next, now, user, completionType, assignee, payloadUpdate))
  }

  def requeue(expectedVersion: AggregateVersion, requeueType: Option[String], assignee: Option[User], payloadUpdate: Option[Patch], score: Option[Long]): Task = {
    checkVersion(expectedVersion)
    checkConflict(status != Ready && status != Archived, s"Cannot requeue a ready or archived task ($aggregateId), but was $status")
    applyTaskRequeued(TaskRequeued(aggregateId, version.next, now, requeueType, assignee, payloadUpdate, score))
  }

  def reschedule(expectedVersion: AggregateVersion, triggerDate: DateTime, assignee: Option[User], rescheduleType: Option[String], payloadUpdate: Option[Patch], score: Option[Long]): Task = {
    checkVersion(expectedVersion)
    checkConflict(status != Archived, s"Cannot reschedule an archived task ($aggregateId)")
    applyTaskRescheduled(TaskRescheduled(aggregateId, version.next, now, triggerDate, assignee, rescheduleType, payloadUpdate, score))
  }

  def unassign(expectedVersion: AggregateVersion, unassignType: Option[String], payloadUpdate: Option[Patch]): Task = {
    checkVersion(expectedVersion)
    checkConflict(status == Waiting || status == Ready, s"Can only unassign a waiting or ready task ($aggregateId), but was $status")
    checkConflict(assignee.isDefined, s"Cannot unassign a task ($aggregateId) that is already unassigned")
    applyTaskUnassigned(TaskUnassigned(aggregateId, version.next, now, unassignType, payloadUpdate))
  }

  def archive(expectedVersion: AggregateVersion): Task = {
    checkVersion(expectedVersion)
    checkConflict(status != Archived, s"Cannot archive a task ($aggregateId) when already archived")
    applyTaskArchived(TaskArchived(aggregateId, version.next, now))
  }

  def markCommitted = copy(uncommittedEvents = Nil)

  def applyEvent = {
    case event: TaskTriggered => applyTaskTriggered(event)
    case event: TaskStarted => applyTaskStarted(event)
    case event: TaskClosed => applyTaskClosed(event)
    case event: TaskPayloadUpdated => applyTaskPayloadUpdated(event)
    case event: TaskRequeued => applyTaskRequeued(event)
    case event: TaskRescheduled => applyTaskRescheduled(event)
    case event: TaskUnassigned => applyTaskUnassigned(event)
    case event: TaskArchived => applyTaskArchived(event)
    case event => unhandled(event)
  }

  private def applyTaskTriggered(event: TaskTriggered) = copy(event :: uncommittedEvents, version = event.aggregateVersion, status = Ready)

  private def applyTaskStarted(event: TaskStarted) = copy(event :: uncommittedEvents, version = event.aggregateVersion, status = Started, assignee = event.assignee, payload = optPayloadUpdate(event.payloadUpdate))

  private def applyTaskClosed(event: TaskClosed) = copy(event :: uncommittedEvents, version = event.aggregateVersion, status = Closed, assignee = event.assignee, payload = optPayloadUpdate(event.payloadUpdate))

  private def applyTaskUnassigned(event: TaskUnassigned) = copy(event :: uncommittedEvents, version = event.aggregateVersion, assignee = None, payload = optPayloadUpdate(event.payloadUpdate))

  private def applyTaskPayloadUpdated(event: TaskPayloadUpdated) = copy(
    event :: uncommittedEvents,
    event.aggregateId,
    version = event.aggregateVersion,
    payload = Payload(event.payloadUpdate.exec(payload.content)),
    score = event.score getOrElse score
  )

  private def applyTaskRequeued(event: TaskRequeued) = copy(event :: uncommittedEvents, version = event.aggregateVersion, status = Ready)

  private def applyTaskRescheduled(event: TaskRescheduled) = copy(event :: uncommittedEvents, version = event.aggregateVersion, status = Waiting, triggerDate = event.triggerDate, assignee = event.assignee)

  private def applyTaskArchived(event: TaskArchived) = copy(event :: uncommittedEvents, version = event.aggregateVersion, status = Archived)

  private def optPayloadUpdate(payloadUpdate: Option[Patch]) = payloadUpdate.map(update => Payload(update.exec(payload.content))).getOrElse(payload)
}