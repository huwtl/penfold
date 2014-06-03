package com.qmetric.penfold.domain.model

import org.specs2.mutable.Specification
import com.qmetric.penfold.domain.event._
import org.joda.time.DateTime
import com.qmetric.penfold.domain.event.TaskCreated
import com.qmetric.penfold.domain.event.TaskTriggered
import com.qmetric.penfold.domain.event.TaskStarted
import com.qmetric.penfold.domain.exceptions.AggregateConflictException
import com.qmetric.penfold.domain.model.patch.Patch

class TaskTest extends Specification {

  val queue = QueueId("abc")

  val concluder = User("user1")

  val conclusionType = "type"

  "create new task" in {
    val createdTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None)
    typesOf(createdTask.uncommittedEvents) must beEqualTo(List(classOf[TaskCreated]))
  }

  "create new future task" in {
    val createdTask = Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty, None)
    typesOf(createdTask.uncommittedEvents) must beEqualTo(List(classOf[FutureTaskCreated]))
  }

  "trigger new future task if trigger date in past" in {
    val createdTask = Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().minusDays(1), Payload.empty, None)
    typesOf(createdTask.uncommittedEvents) must beEqualTo(List(classOf[TaskTriggered], classOf[FutureTaskCreated]))
  }

  "trigger future task" in {
    val readyTask = Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty, None).trigger
    typesOf(readyTask.uncommittedEvents) must beEqualTo(List(classOf[TaskTriggered], classOf[FutureTaskCreated]))
  }

  "ensure only waiting tasks can be triggered" in {
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).trigger must throwA[AggregateConflictException]
    Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty, None).trigger.trigger must throwA[AggregateConflictException]
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start(None).trigger must throwA[AggregateConflictException]
  }

  "start task" in {
    val startedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start(None)
    typesOf(startedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskStarted], classOf[TaskCreated]))
  }

  "ensure only ready tasks can be started" in {
    Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty, None).start(None) must throwA[AggregateConflictException]
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start(None).start(None) must throwA[AggregateConflictException]
  }

  "close task" in {
    val closeTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).close(Some(concluder), Some(conclusionType))
    typesOf(closeTask.uncommittedEvents) must beEqualTo(List(classOf[TaskClosed], classOf[TaskCreated]))
  }

  "ensure archived tasks cannot be closed" in {
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).archive.close() must throwA[AggregateConflictException]
  }

  "requeue task" in {
    val requeuedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start(Some(Assignee("user"))).requeue
    typesOf(requeuedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskRequeued], classOf[TaskStarted], classOf[TaskCreated]))
    requeuedTask.assignee must beNone
  }

  "ensure waiting, ready, archived tasks cannot be requeued" in {
    Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty, None).requeue must throwA[AggregateConflictException]
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).requeue must throwA[AggregateConflictException]
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).archive.requeue must throwA[AggregateConflictException]
  }

  "update task payload" in {
    val updatedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).updatePayload(AggregateVersion.init, Patch(Nil), None, None)
    typesOf(updatedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskPayloadUpdated], classOf[TaskCreated]))
  }

  "prevent concurrent task payload updates"in {
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None)
      .updatePayload(AggregateVersion.init, Patch(Nil), None, None)
      .updatePayload(AggregateVersion.init, Patch(Nil), None, None) must throwA[AggregateConflictException]
  }

  "ensure closed, archived tasks cannot accept updated payload" in {
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start(None).close().updatePayload(AggregateVersion.init.next.next, Patch(Nil), None, None) must throwA[RuntimeException]
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).archive.updatePayload(AggregateVersion.init.next, Patch(Nil), None, None) must throwA[RuntimeException]
  }

  "archive task" in {
    val archivedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).archive
    typesOf(archivedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskArchived], classOf[TaskCreated]))
  }

  "ensure cannot archive an already archived task" in {
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).archive.archive must throwA[AggregateConflictException]
  }

  private def typesOf(events: List[Event]) = {
    events.map(_.getClass)
  }
}
