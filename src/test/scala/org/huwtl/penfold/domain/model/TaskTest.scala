package org.huwtl.penfold.domain.model

import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.event._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.event.TaskCreated
import org.huwtl.penfold.domain.event.TaskTriggered
import org.huwtl.penfold.domain.event.TaskStarted

class TaskTest extends Specification {

  val queue = QueueId("abc")

  "create new task" in {
    val createdTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty)
    typesOf(createdTask.uncommittedEvents) must beEqualTo(List(classOf[TaskCreated]))
  }

  "create new future task" in {
    val createdTask = Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty)
    typesOf(createdTask.uncommittedEvents) must beEqualTo(List(classOf[FutureTaskCreated]))
  }

  "trigger new future task if trigger date in past" in {
    val createdTask = Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().minusDays(1), Payload.empty)
    typesOf(createdTask.uncommittedEvents) must beEqualTo(List(classOf[TaskTriggered], classOf[FutureTaskCreated]))
  }

  "trigger future task" in {
    val readyTask = Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty).trigger()
    typesOf(readyTask.uncommittedEvents) must beEqualTo(List(classOf[TaskTriggered], classOf[FutureTaskCreated]))
  }

  "ensure only waiting tasks can be triggered" in {
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty).trigger() must throwA[RuntimeException]
    Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty).trigger().trigger() must throwA[RuntimeException]
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty).start(queue).trigger() must throwA[RuntimeException]
  }

  "start task" in {
    val startedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty).start(queue)
    typesOf(startedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskStarted], classOf[TaskCreated]))
  }

  "ensure only ready tasks can be started" in {
    Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty).start(queue) must throwA[RuntimeException]
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty).start(queue).start(queue) must throwA[RuntimeException]
  }

  "cancel task" in {
    val startedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty).cancel(queue)
    typesOf(startedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskCancelled], classOf[TaskCreated]))
  }

  "complete task" in {
    val startedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty).start(queue).complete(queue)
    typesOf(startedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskCompleted], classOf[TaskStarted], classOf[TaskCreated]))
  }

  "ensure only started tasks can be completed" in {
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty).complete(queue) must throwA[RuntimeException]
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty).start(queue).cancel(queue).complete(queue) must throwA[RuntimeException]
  }

  private def typesOf(events: List[Event]) = {
    events.map(_.getClass)
  }
}
