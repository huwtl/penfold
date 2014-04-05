package org.huwtl.penfold.domain.model

import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.event._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.domain.event.JobTriggered
import org.huwtl.penfold.domain.event.JobStarted

class JobTest extends Specification {

  val queue = QueueId("abc")

  "create new job" in {
    val createdJob = Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload.empty)
    typesOf(createdJob.uncommittedEvents) must beEqualTo(List(classOf[JobTriggered], classOf[JobCreated]))
  }

  "create new future job" in {
    val createdJob = Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), DateTime.now().plusHours(1), Payload.empty)
    typesOf(createdJob.uncommittedEvents) must beEqualTo(List(classOf[JobCreated]))
  }

  "trigger new future job if trigger date in past" in {
    val createdJob = Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), DateTime.now().minusDays(1), Payload.empty)
    typesOf(createdJob.uncommittedEvents) must beEqualTo(List(classOf[JobTriggered], classOf[JobCreated]))
  }

  "trigger future job" in {
    val readyJob = Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), DateTime.now().plusHours(1), Payload.empty).trigger()
    typesOf(readyJob.uncommittedEvents) must beEqualTo(List(classOf[JobTriggered], classOf[JobCreated]))
  }

  "ensure only waiting jobs can be triggered" in {
    Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload.empty).trigger().trigger() must throwA[RuntimeException]
    Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload.empty).start(queue).trigger() must throwA[RuntimeException]
  }

  "start job" in {
    val startedJob = Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload.empty).start(queue)
    typesOf(startedJob.uncommittedEvents) must beEqualTo(List(classOf[JobStarted], classOf[JobTriggered], classOf[JobCreated]))
  }

  "ensure only ready jobs can be started" in {
    Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), DateTime.now().plusHours(1), Payload.empty).start(queue) must throwA[RuntimeException]
    Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload.empty).start(queue).start(queue) must throwA[RuntimeException]
  }

  "cancel job" in {
    val startedJob = Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload.empty).cancel(queue)
    typesOf(startedJob.uncommittedEvents) must beEqualTo(List(classOf[JobCancelled], classOf[JobTriggered], classOf[JobCreated]))
  }

  "complete job" in {
    val startedJob = Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload.empty).start(queue).complete(queue)
    typesOf(startedJob.uncommittedEvents) must beEqualTo(List(classOf[JobCompleted], classOf[JobStarted], classOf[JobTriggered], classOf[JobCreated]))
  }

  "ensure only started jobs can be completed" in {
    Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload.empty).complete(queue) must throwA[RuntimeException]
    Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload.empty).start(queue).cancel(queue).complete(queue) must throwA[RuntimeException]
  }

  private def typesOf(events: List[Event]) = {
    events.map(_.getClass)
  }
}
