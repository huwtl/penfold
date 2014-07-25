package com.qmetric.penfold.domain.model

import org.specs2.mutable.Specification
import com.qmetric.penfold.domain.event._
import org.joda.time.DateTime
import com.qmetric.penfold.domain.event.TaskCreated
import com.qmetric.penfold.domain.event.TaskTriggered
import com.qmetric.penfold.domain.event.TaskStarted
import com.qmetric.penfold.domain.exceptions.AggregateConflictException
import com.qmetric.penfold.domain.model.patch.Patch
import scala.None
import com.qmetric.penfold.support.TestModel

class TaskTest extends Specification {

  val queue = QueueId("abc")

  val concluder = User("user1")

  val conclusionType = "type"

  val assignee = User("user")

  "task creation" should {
    "create new task" in {
      val createdTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None)
      typesOf(createdTask.uncommittedEvents) must beEqualTo(List(classOf[TaskCreated]))
    }

    "create new future task" in {
      val createdTask = Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty, None)
      typesOf(createdTask.uncommittedEvents) must beEqualTo(List(classOf[FutureTaskCreated]))
    }
  }

  "task triggering" should {
    "trigger new future task if trigger date in past" in {
      val createdTask = Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().minusDays(1), Payload.empty, None)
      typesOf(createdTask.uncommittedEvents) must beEqualTo(List(classOf[TaskTriggered], classOf[FutureTaskCreated]))
    }

    "trigger future task" in {
      val readyTask = Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty, None).trigger(AggregateVersion.init)
      typesOf(readyTask.uncommittedEvents) must beEqualTo(List(classOf[TaskTriggered], classOf[FutureTaskCreated]))
    }

    "ensure only waiting tasks can be triggered" in {
      Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).trigger(TestModel.version) must throwA[IllegalStateException]
      Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty, None).trigger(TestModel.version).trigger(TestModel.version.next) must throwA[IllegalStateException]
      Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start(TestModel.version, None, None).trigger(TestModel.version.next) must throwA[IllegalStateException]
    }
  }

  "task starting" should {
    "start task" in {
      val startedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start(TestModel.version, None, None)
      typesOf(startedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskStarted], classOf[TaskCreated]))
    }

    "ensure only ready tasks can be started" in {
      Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty, None).start(TestModel.version, None, None) must throwA[IllegalStateException]
      Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start(TestModel.version, None, None).start(TestModel.version.next, None, None) must throwA[IllegalStateException]
    }
  }

  "task closure" should {
    "close task" in {
      val closeTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).close(TestModel.version, Some(concluder), Some(conclusionType), None, None)
      typesOf(closeTask.uncommittedEvents) must beEqualTo(List(classOf[TaskClosed], classOf[TaskCreated]))
    }

    "ensure archived tasks cannot be closed" in {
      Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).archive(TestModel.version).close(TestModel.version.next, None, None, None, None) must throwA[IllegalStateException]
    }
  }

  "task requeuing" should {
    "requeue task" in {
      val requeuedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start(TestModel.version, Some(assignee), None).requeue(TestModel.version.next, None, None, None, None)
      typesOf(requeuedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskRequeued], classOf[TaskStarted], classOf[TaskCreated]))
    }

    "ensure waiting, ready, archived tasks cannot be requeued" in {
      Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty, None).requeue(TestModel.version, None, None, None, None) must throwA[IllegalStateException]
      Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).requeue(TestModel.version, None, None, None, None) must throwA[IllegalStateException]
      Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).archive(TestModel.version).requeue(TestModel.version.next, None, None, None, None) must throwA[IllegalStateException]
    }
  }

  "task rescheduling" should {
    "reschedule task" in {
      val rescheduleTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start(TestModel.version, Some(assignee), None).reschedule(TestModel.version.next, DateTime.now().plusHours(1), Some(assignee), Some("type"), None, None)
      typesOf(rescheduleTask.uncommittedEvents) must beEqualTo(List(classOf[TaskRescheduled], classOf[TaskStarted], classOf[TaskCreated]))
    }

    "ensure archived tasks cannot be rescheduled" in {
      Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).archive(TestModel.version).reschedule(TestModel.version.next, DateTime.now().plusHours(1), None, None, None, None) must throwA[IllegalStateException]
    }
  }

  "task paylod updating" should {
    val readyTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None)

    "update task payload" in {
      val updatedTask = readyTask.updatePayload(AggregateVersion.init, Patch(Nil), None, None)
      typesOf(updatedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskPayloadUpdated], classOf[TaskCreated]))
    }

    "prevent concurrent task payload updates" in {
      readyTask
        .updatePayload(AggregateVersion.init, Patch(Nil), None, None)
        .updatePayload(AggregateVersion.init, Patch(Nil), None, None) must throwA[AggregateConflictException]
    }

    "ensure closed, archived tasks cannot accept updated payload" in {
      readyTask
        .close(TestModel.version, None, None, None, None)
        .updatePayload(TestModel.version.next, Patch(Nil), None, None) must throwA[IllegalStateException]

      readyTask
        .archive(TestModel.version)
        .updatePayload(TestModel.version.next, Patch(Nil), None, None) must throwA[IllegalStateException]
    }
  }

  "task archiving" should {
    "archive task" in {
      val archivedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).archive(TestModel.version)
      typesOf(archivedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskArchived], classOf[TaskCreated]))
    }

    "ensure cannot archive an already archived task" in {
      Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).archive(TestModel.version).archive(TestModel.version.next) must throwA[IllegalStateException]
    }
  }

  "task unassignment" should {
    "unassign ready task" in {
      val unassignedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None)
        .start(TestModel.version, Some(assignee), None)
        .requeue(TestModel.version.next, None, Some(assignee), None, None)
        .unassign(TestModel.version.next.next, None, None)

      typesOf(unassignedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskUnassigned], classOf[TaskRequeued], classOf[TaskStarted], classOf[TaskCreated]))
    }

    "unassign waiting task" in {
      val unassignedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None)
        .start(TestModel.version, Some(assignee), None)
        .reschedule(TestModel.version.next, TestModel.triggerDate, Some(assignee), None, None, None)
        .unassign(TestModel.version.next.next, None, None)

      typesOf(unassignedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskUnassigned], classOf[TaskRescheduled], classOf[TaskStarted], classOf[TaskCreated]))
    }

    "only ready and waiting tasks can be unassigned" in {
      val task = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None)
      task.archive(TestModel.version).unassign(TestModel.version.next, None, None) must throwA[IllegalStateException]
      task.start(TestModel.version, None, None).unassign(TestModel.version.next, None, None) must throwA[IllegalStateException]
      task.close(TestModel.version, None, None, None, None).unassign(TestModel.version.next, None, None) must throwA[IllegalStateException]
    }
  }

  "prevent concurrent task updates" in {
    val waitingTaskAtVersion1 = Task.create(AggregateId("1"), QueueBinding(queue), TestModel.triggerDate, Payload.empty, None)
    val readyTaskAtVersion3 = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start(TestModel.version, None, None).requeue(TestModel.version.next, None, None, None, None)
    val startedTaskAtVersion2 = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start(TestModel.version, None, None)

    waitingTaskAtVersion1.trigger(AggregateVersion(1)) must throwA[AggregateConflictException]
    waitingTaskAtVersion1.archive(AggregateVersion(1)) must throwA[AggregateConflictException]
    readyTaskAtVersion3.start(AggregateVersion(2), None, None) must throwA[AggregateConflictException]
    readyTaskAtVersion3.reschedule(AggregateVersion(2), TestModel.triggerDate, None, None, None, None) must throwA[AggregateConflictException]
    readyTaskAtVersion3.close(AggregateVersion(2), None, None, None, None) must throwA[AggregateConflictException]
    startedTaskAtVersion2.requeue(AggregateVersion(1), None, None, None, None) must throwA[AggregateConflictException]
    startedTaskAtVersion2.unassign(AggregateVersion(1), None, None) must throwA[AggregateConflictException]
  }

  private def typesOf(events: List[Event]) = events.map(_.getClass)
}
