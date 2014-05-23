package org.huwtl.penfold.domain.model

import org.joda.time.DateTime
import org.joda.time.DateTime.now
import org.huwtl.penfold.domain.event._
import org.huwtl.penfold.domain.event.TaskCreated
import org.huwtl.penfold.domain.model.Status._
import org.huwtl.penfold.domain.model.patch.Patch

object Task extends AggregateFactory {
  def create(aggregateId: AggregateId, queueBinding: QueueBinding, payload: Payload, score: Option[Long]) = {
    val currentDateTime = now
    val scoreValue = score getOrElse currentDateTime.getMillis
    applyTaskCreated(TaskCreated(aggregateId, AggregateVersion.init, currentDateTime, queueBinding, currentDateTime, payload, scoreValue))
  }

  def create(aggregateId: AggregateId, queueBinding: QueueBinding, triggerDate: DateTime, payload: Payload, score: Option[Long]) = {
    val scoreValue = score getOrElse triggerDate.getMillis
    val createdTask = applyFutureTaskCreated(FutureTaskCreated(aggregateId, AggregateVersion.init, now, queueBinding, triggerDate, payload, scoreValue))
    if (createdTask.triggerDate.isAfterNow) createdTask else createdTask.trigger
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
    None,
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
                assignee: Option[Assignee],
                concluder: Option[User],
                conclusionType: Option[String],
                queueBinding: QueueBinding,
                status: Status,
                triggerDate: DateTime,
                payload: Payload,
                score: Long) extends AggregateRoot {

  override def aggregateType = AggregateType.Task

  def trigger: Task = {
    checkConflict(status == Waiting, s"Can only trigger a waiting task but was $status")
    applyTaskTriggered(TaskTriggered(aggregateId, version.next, now))
  }

  def updatePayload(expectedVersion: AggregateVersion, payloadUpdate: Patch, updateType: Option[String], score: Option[Long]): Task = {
    checkVersion(expectedVersion)
    require(status != Completed && status != Cancelled && status != Archived, s"Cannot update payload for completed/cancelled/archived task but was $status")
    applyTaskPayloadUpdated(TaskPayloadUpdated(aggregateId, version.next, now, payloadUpdate, updateType, score))
  }

  def start(assignee: Option[Assignee]): Task = {
    checkConflict(status == Ready, s"Can only start a task that is ready but was $status")
    applyTaskStarted(TaskStarted(aggregateId, version.next, now, assignee))
  }

  def cancel(user: Option[User] = None, cancellationType: Option[String] = None): Task = {
    applyTaskCancelled(TaskCancelled(aggregateId, version.next, now, user, cancellationType))
  }

  def complete(user: Option[User] = None, completionType: Option[String] = None): Task = {
    checkConflict(status == Started, s"Can only complete a started task but was $status")
    applyTaskCompleted(TaskCompleted(aggregateId, version.next, now, user, completionType))
  }

  def requeue: Task = {
    checkConflict(status != Waiting && status != Ready && status != Archived, s"Cannot requeue task from status $status")
    applyTaskRequeued(TaskRequeued(aggregateId, version.next, now))
  }

  def archive: Task = {
    checkConflict(status != Archived, s"Cannot archive task when already archived")
    applyTaskArchived(TaskArchived(aggregateId, version.next, now))
  }

  def markCommitted = copy(uncommittedEvents = Nil)

  def applyEvent = {
    case event: TaskTriggered => applyTaskTriggered(event)
    case event: TaskStarted => applyTaskStarted(event)
    case event: TaskCancelled => applyTaskCancelled(event)
    case event: TaskCompleted => applyTaskCompleted(event)
    case event: TaskPayloadUpdated => applyTaskPayloadUpdated(event)
    case event: TaskRequeued => applyTaskRequeued(event)
    case event: TaskArchived => applyTaskArchived(event)
    case event => unhandled(event)
  }

  private def applyTaskTriggered(event: TaskTriggered) = copy(event :: uncommittedEvents, version = event.aggregateVersion, status = Ready)

  private def applyTaskStarted(event: TaskStarted) = copy(event :: uncommittedEvents, version = event.aggregateVersion, status = Started, assignee = event.assignee)

  private def applyTaskCancelled(event: TaskCancelled) = copy(event :: uncommittedEvents, event.aggregateId, version = event.aggregateVersion, status = Cancelled, concluder = event.concluder, conclusionType = event.conclusionType)

  private def applyTaskCompleted(event: TaskCompleted) = copy(event :: uncommittedEvents, event.aggregateId, version = event.aggregateVersion, status = Completed, concluder = event.concluder, conclusionType = event.conclusionType)

  private def applyTaskPayloadUpdated(event: TaskPayloadUpdated) = copy(
    event :: uncommittedEvents,
    event.aggregateId,
    version = event.aggregateVersion,
    payload = Payload(event.payloadUpdate.exec(payload.content)),
    score = event.score getOrElse score
  )

  private def applyTaskRequeued(event: TaskRequeued) = copy(event :: uncommittedEvents, event.aggregateId, version = event.aggregateVersion, status = Ready, assignee = None)

  private def applyTaskArchived(event: TaskArchived) = copy(event :: uncommittedEvents, event.aggregateId, version = event.aggregateVersion, status = Archived)
}