package org.huwtl.penfold.domain.model

import org.joda.time.DateTime
import org.joda.time.DateTime.now
import org.huwtl.penfold.domain.event._
import org.huwtl.penfold.domain.event.TaskCreated
import org.huwtl.penfold.domain.model.Status._

object Task extends AggregateFactory {
  def create(aggregateId: AggregateId, binding: Binding, payload: Payload) = {
    val currentDateTime = now
    applyTaskCreated(TaskCreated(aggregateId, AggregateVersion.init, currentDateTime, binding, currentDateTime, payload))
  }

  def create(aggregateId: AggregateId, binding: Binding, triggerDate: DateTime, payload: Payload) = {
    val createdTask = applyFutureTaskCreated(FutureTaskCreated(aggregateId, AggregateVersion.init, now, binding, triggerDate, payload))
    if (createdTask.triggerDate.isAfterNow) createdTask else createdTask.trigger()
  }

  def applyEvent = {
    case event: TaskCreated => applyTaskCreated(event)
    case event: FutureTaskCreated => applyFutureTaskCreated(event)
    case event => unhandled(event)
  }

  private def applyTaskCreated(event: TaskCreated) = Task(
    event :: Nil,
    event.aggregateId,
    event.aggregateVersion,
    event.created,
    event.binding,
    Ready,
    event.triggerDate,
    event.payload
  )

  private def applyFutureTaskCreated(event: FutureTaskCreated) = Task(
    event :: Nil,
    event.aggregateId,
    event.aggregateVersion,
    event.created,
    event.binding,
    Waiting,
    event.triggerDate,
    event.payload
  )
}

case class Task(uncommittedEvents: List[Event],
                        aggregateId: AggregateId,
                        version: AggregateVersion,
                        created: DateTime,
                        binding: Binding,
                        status: Status,
                        triggerDate: DateTime,
                        payload: Payload) extends AggregateRoot {

  override def aggregateType = AggregateType.Task

  def trigger(): Task = {
    require(status == Waiting, s"Can only trigger a waiting task but was $status")
    applyTaskTriggered(TaskTriggered(aggregateId, version.next, now, binding.queues.map(_.id)))
  }

  def start(queue: QueueId): Task = {
    require(status == Ready, s"Can only start a task that is ready but was $status")
    applyTaskStarted(TaskStarted(aggregateId, version.next, now, queue))
  }

  def cancel(queue: QueueId): Task = {
    applyTaskCancelled(TaskCancelled(aggregateId, version.next, now, List(queue)))
  }

  def complete(queue: QueueId): Task = {
    require(status == Started, s"Can only complete a started task but was $status")
    applyTaskCompleted(TaskCompleted(aggregateId, version.next, now, queue))
  }

  def markCommitted = copy(uncommittedEvents = Nil)

  def applyEvent = {
    case event: TaskTriggered => applyTaskTriggered(event)
    case event: TaskStarted => applyTaskStarted(event)
    case event: TaskCancelled => applyTaskCancelled(event)
    case event: TaskCompleted => applyTaskCompleted(event)
    case event => unhandled(event)
  }

  private def applyTaskTriggered(event: TaskTriggered) = copy(event :: uncommittedEvents, version = version.next, status = Ready)

  private def applyTaskStarted(event: TaskStarted) = copy(event :: uncommittedEvents, version = version.next, status = Started)

  private def applyTaskCancelled(event: TaskCancelled) = copy(event :: uncommittedEvents, event.aggregateId, version = version.next, status = Cancelled)

  private def applyTaskCompleted(event: TaskCompleted) = copy(event :: uncommittedEvents, event.aggregateId, version = version.next, status = Completed)
}