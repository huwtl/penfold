package org.huwtl.penfold.domain.model

import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.event._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.event.TaskCreated
import org.huwtl.penfold.domain.event.TaskTriggered
import org.huwtl.penfold.domain.event.TaskStarted
import org.huwtl.penfold.domain.exceptions.AggregateConflictException
import org.huwtl.penfold.domain.patch.Patch

class TaskTest extends Specification {

  val queue = QueueId("abc")

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
    val readyTask = Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty, None).trigger()
    typesOf(readyTask.uncommittedEvents) must beEqualTo(List(classOf[TaskTriggered], classOf[FutureTaskCreated]))
  }

  "ensure only waiting tasks can be triggered" in {
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).trigger() must throwA[RuntimeException]
    Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty, None).trigger().trigger() must throwA[RuntimeException]
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start().trigger() must throwA[RuntimeException]
  }

  "start task" in {
    val startedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start()
    typesOf(startedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskStarted], classOf[TaskCreated]))
  }

  "ensure only ready tasks can be started" in {
    Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty, None).start() must throwA[RuntimeException]
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start().start() must throwA[RuntimeException]
  }

  "cancel task" in {
    val startedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).cancel()
    typesOf(startedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskCancelled], classOf[TaskCreated]))
  }

  "complete task" in {
    val startedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start().complete()
    typesOf(startedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskCompleted], classOf[TaskStarted], classOf[TaskCreated]))
  }

  "ensure only started tasks can be completed" in {
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).complete() must throwA[RuntimeException]
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start().cancel().complete() must throwA[RuntimeException]
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

  "ensure completed or cancelled tasks cannot accept updated payload" in {
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).complete().updatePayload(AggregateVersion.init, Patch(Nil), None, None) must throwA[RuntimeException]
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).cancel().updatePayload(AggregateVersion.init.next, Patch(Nil), None, None) must throwA[RuntimeException]
  }

  private def typesOf(events: List[Event]) = {
    events.map(_.getClass)
  }
}
