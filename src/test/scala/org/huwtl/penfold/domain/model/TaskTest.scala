package org.huwtl.penfold.domain.model

import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.event._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.event.TaskCreated
import org.huwtl.penfold.domain.event.TaskTriggered
import org.huwtl.penfold.domain.event.TaskStarted
import org.huwtl.penfold.domain.exceptions.AggregateConflictException
import org.huwtl.penfold.domain.model.patch.Patch
import scala.None
import org.huwtl.penfold.support.TestModel
import org.huwtl.penfold.domain.model.CloseResultType.Success

class TaskTest extends Specification {

  val queue = QueueId("abc")

  val user = User("user1")

  val closeReason = "type"

  "task creation" should {
    "create new task" in {
      val createdTask = Task.create(AggregateId("1"), queue, Payload.empty, None)
      typesOf(createdTask.uncommittedEvents) must beEqualTo(List(classOf[TaskCreated]))
    }

    "create new future task" in {
      val createdTask = Task.create(AggregateId("1"), queue, DateTime.now().plusHours(1), Payload.empty, None)
      typesOf(createdTask.uncommittedEvents) must beEqualTo(List(classOf[FutureTaskCreated]))
    }
  }

  "task triggering" should {
    "trigger new future task if trigger date in past" in {
      val createdTask = Task.create(AggregateId("1"), queue, DateTime.now().minusDays(1), Payload.empty, None)
      typesOf(createdTask.uncommittedEvents) must beEqualTo(List(classOf[TaskTriggered], classOf[FutureTaskCreated]))
    }

    "trigger future task" in {
      val readyTask = Task.create(AggregateId("1"), queue, DateTime.now().plusHours(1), Payload.empty, None).trigger(AggregateVersion.init)
      typesOf(readyTask.uncommittedEvents) must beEqualTo(List(classOf[TaskTriggered], classOf[FutureTaskCreated]))
    }

    "ensure only waiting tasks can be triggered" in {
      Task.create(AggregateId("1"), queue, Payload.empty, None).trigger(TestModel.version) must throwA[AggregateConflictException]
      Task.create(AggregateId("1"), queue, DateTime.now().plusHours(1), Payload.empty, None).trigger(TestModel.version).trigger(TestModel.version.next) must throwA[AggregateConflictException]
      Task.create(AggregateId("1"), queue, Payload.empty, None).start(TestModel.version, None, None).trigger(TestModel.version.next) must throwA[AggregateConflictException]
    }
  }

  "task starting" should {
    "start task" in {
      val startedTask = Task.create(AggregateId("1"), queue, Payload.empty, None).start(TestModel.version, None, None)
      typesOf(startedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskStarted], classOf[TaskCreated]))
    }

    "ensure only ready tasks can be started" in {
      Task.create(AggregateId("1"), queue, DateTime.now().plusHours(1), Payload.empty, None).start(TestModel.version, None, None) must throwA[AggregateConflictException]
      Task.create(AggregateId("1"), queue, Payload.empty, None).start(TestModel.version, None, None).start(TestModel.version.next, None, None) must throwA[AggregateConflictException]
    }
  }

  "task closure" should {
    "close task" in {
      val closeTask = Task.create(AggregateId("1"), queue, Payload.empty, None).close(TestModel.version, Some(user), Some(closeReason), Some(Success), None)
      typesOf(closeTask.uncommittedEvents) must beEqualTo(List(classOf[TaskClosed], classOf[TaskCreated]))
    }

    "ensure archived tasks cannot be closed" in {
      Task.create(AggregateId("1"), queue, Payload.empty, None).archive(TestModel.version).close(TestModel.version.next, None, None, None, None) must throwA[AggregateConflictException]
    }

    "ensure cancelled tasks cannot be closed" in {
      Task.create(AggregateId("1"), queue, Payload.empty, None).cancel(TestModel.version, None, None, None).close(TestModel.version.next, None, None, None, None) must throwA[AggregateConflictException]
    }
  }

  "task cancellation" should {
    "cancel task" in {
      val cancelledTask = Task.create(AggregateId("1"), queue, Payload.empty, None).cancel(TestModel.version, Some(user), Some(closeReason), None)
      typesOf(cancelledTask.uncommittedEvents) must beEqualTo(List(classOf[TaskCancelled], classOf[TaskCreated]))
    }

    "ensure archived tasks cannot be cancelled" in {
      Task.create(AggregateId("1"), queue, Payload.empty, None).archive(TestModel.version).cancel(TestModel.version.next, None, None, None) must throwA[AggregateConflictException]
    }

    "ensure closed tasks cannot be cancelled" in {
      Task.create(AggregateId("1"), queue, Payload.empty, None).close(TestModel.version, None, None, None, None).cancel(TestModel.version.next, None, None, None) must throwA[AggregateConflictException]
    }

    "ensure cancelled tasks cannot be cancelled again" in {
      Task.create(AggregateId("1"), queue, Payload.empty, None).cancel(TestModel.version, None, None, None).cancel(TestModel.version.next, None, None, None) must throwA[AggregateConflictException]
    }
  }

  "task requeuing" should {
    "requeue task" in {
      val requeuedTask = Task.create(AggregateId("1"), queue, Payload.empty, None).start(TestModel.version, Some(user), None).requeue(TestModel.version.next, None, None, None, None)
      typesOf(requeuedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskRequeued], classOf[TaskStarted], classOf[TaskCreated]))
    }

    "requeue waiting task" in {
      val requeuedTask = Task.create(AggregateId("1"), queue, DateTime.now().plusHours(1), Payload.empty, None).requeue(TestModel.version, None, None, None, None)
      typesOf(requeuedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskRequeued], classOf[FutureTaskCreated]))
    }

    "ensure ready, archived, closed tasks cannot be requeued" in {
      Task.create(AggregateId("1"), queue, Payload.empty, None).requeue(TestModel.version, None, None, None, None) must throwA[AggregateConflictException]
      Task.create(AggregateId("1"), queue, Payload.empty, None).archive(TestModel.version).requeue(TestModel.version.next, None, None, None, None) must throwA[AggregateConflictException]
      Task.create(AggregateId("1"), queue, Payload.empty, None).cancel(TestModel.version, None, None, None).requeue(TestModel.version.next, None, None, None, None) must throwA[AggregateConflictException]
    }
  }

  "task rescheduling" should {
    "reschedule task" in {
      val rescheduleTask = Task.create(AggregateId("1"), queue, Payload.empty, None).start(TestModel.version, Some(user), None).reschedule(TestModel.version.next, DateTime.now().plusHours(1), Some(user), Some("type"), None, None)
      typesOf(rescheduleTask.uncommittedEvents) must beEqualTo(List(classOf[TaskRescheduled], classOf[TaskStarted], classOf[TaskCreated]))
    }

    "ensure archived tasks cannot be rescheduled" in {
      Task.create(AggregateId("1"), queue, Payload.empty, None).archive(TestModel.version).reschedule(TestModel.version.next, DateTime.now().plusHours(1), None, None, None, None) must throwA[AggregateConflictException]
    }

    "ensure cancelled tasks cannot be rescheduled" in {
      Task.create(AggregateId("1"), queue, Payload.empty, None).cancel(TestModel.version, None, None, None).reschedule(TestModel.version.next, DateTime.now().plusHours(1), None, None, None, None) must throwA[AggregateConflictException]
    }
  }

  "task paylod updating" should {
    val readyTask = Task.create(AggregateId("1"), queue, Payload.empty, None)

    "update task payload" in {
      val updatedTask = readyTask.updatePayload(AggregateVersion.init, Patch(Nil), None, None)
      typesOf(updatedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskPayloadUpdated], classOf[TaskCreated]))
    }

    "prevent concurrent task payload updates" in {
      readyTask
        .updatePayload(AggregateVersion.init, Patch(Nil), None, None)
        .updatePayload(AggregateVersion.init, Patch(Nil), None, None) must throwA[AggregateConflictException]
    }

    "ensure closed, archived, cancelled tasks cannot accept updated payload" in {
      readyTask
        .close(TestModel.version, None, None, None, None)
        .updatePayload(TestModel.version.next, Patch(Nil), None, None) must throwA[AggregateConflictException]

      readyTask
        .archive(TestModel.version)
        .updatePayload(TestModel.version.next, Patch(Nil), None, None) must throwA[AggregateConflictException]

      readyTask
        .cancel(TestModel.version, None, None, None)
        .updatePayload(TestModel.version.next, Patch(Nil), None, None) must throwA[AggregateConflictException]
    }
  }

  "task archiving" should {
    "archive task" in {
      val archivedTask = Task.create(AggregateId("1"), queue, Payload.empty, None).archive(TestModel.version)
      typesOf(archivedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskArchived], classOf[TaskCreated]))
    }

    "ensure cannot archive an already archived task" in {
      Task.create(AggregateId("1"), queue, Payload.empty, None).archive(TestModel.version).archive(TestModel.version.next) must throwA[AggregateConflictException]
    }
  }

  "task unassignment" should {
    "unassign ready task" in {
      val unassignedTask = Task.create(AggregateId("1"), queue, Payload.empty, None)
        .start(TestModel.version, Some(user), None)
        .requeue(TestModel.version.next, None, Some(user), None, None)
        .unassign(TestModel.version.next.next, None, None)

      typesOf(unassignedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskUnassigned], classOf[TaskRequeued], classOf[TaskStarted], classOf[TaskCreated]))
    }

    "unassign waiting task" in {
      val unassignedTask = Task.create(AggregateId("1"), queue, Payload.empty, None)
        .start(TestModel.version, Some(user), None)
        .reschedule(TestModel.version.next, TestModel.triggerDate, Some(user), None, None, None)
        .unassign(TestModel.version.next.next, None, None)

      typesOf(unassignedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskUnassigned], classOf[TaskRescheduled], classOf[TaskStarted], classOf[TaskCreated]))
    }

    "only ready and waiting tasks can be unassigned" in {
      val task = Task.create(AggregateId("1"), queue, Payload.empty, None)
      task.archive(TestModel.version).unassign(TestModel.version.next, None, None) must throwA[AggregateConflictException]
      task.start(TestModel.version, None, None).unassign(TestModel.version.next, None, None) must throwA[AggregateConflictException]
      task.close(TestModel.version, None, None, None, None).unassign(TestModel.version.next, None, None) must throwA[AggregateConflictException]
    }

    "only assigned tasks can be unassigned" in {
      Task.create(AggregateId("1"), queue, Payload.empty, None)
        .start(TestModel.version, None, None)
        .reschedule(TestModel.version.next, TestModel.triggerDate, None, None, None, None)
        .unassign(TestModel.version.next.next, None, None) must throwA[AggregateConflictException]
    }
  }

  "prevent concurrent task updates" in {
    val waitingTaskAtVersion1 = Task.create(AggregateId("1"), queue, TestModel.triggerDate, Payload.empty, None)
    val readyTaskAtVersion3 = Task.create(AggregateId("1"), queue, Payload.empty, None).start(TestModel.version, None, None).requeue(TestModel.version.next, None, None, None, None)
    val startedTaskAtVersion2 = Task.create(AggregateId("1"), queue, Payload.empty, None).start(TestModel.version, None, None)

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
