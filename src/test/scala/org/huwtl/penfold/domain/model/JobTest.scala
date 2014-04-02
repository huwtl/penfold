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
    val createdJob = Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload(Map()))
    typesOf(createdJob.uncommittedEvents) must beEqualTo(List(classOf[JobCreated]))
  }

  "create new future job" in {
    val createdJob = Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), DateTime.now(), Payload(Map()))
    typesOf(createdJob.uncommittedEvents) must beEqualTo(List(classOf[JobCreated]))
  }

  "trigger job" in {
    val readyJob = Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload(Map())).trigger()
    typesOf(readyJob.uncommittedEvents) must beEqualTo(List(classOf[JobTriggered], classOf[JobCreated]))
  }

  "ensure only waiting jobs can be triggered" in {
    Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload(Map())).trigger().trigger() must throwA[RuntimeException]
    Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload(Map())).trigger().start(queue).trigger() must throwA[RuntimeException]
  }

  "start job" in {
    val startedJob = Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload(Map())).trigger().start(queue)
    typesOf(startedJob.uncommittedEvents) must beEqualTo(List(classOf[JobStarted], classOf[JobTriggered], classOf[JobCreated]))
  }

  "ensure only ready jobs can be started" in {
    Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload(Map())).start(queue) must throwA[RuntimeException]
    Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload(Map())).trigger().start(queue).start(queue) must throwA[RuntimeException]
  }

  "cancel job" in {
    val startedJob = Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload(Map())).trigger().cancel(queue)
    typesOf(startedJob.uncommittedEvents) must beEqualTo(List(classOf[JobCancelled], classOf[JobTriggered], classOf[JobCreated]))
  }

  "complete job" in {
    val startedJob = Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload(Map())).trigger().start(queue).complete(queue)
    typesOf(startedJob.uncommittedEvents) must beEqualTo(List(classOf[JobCompleted], classOf[JobStarted], classOf[JobTriggered], classOf[JobCreated]))
  }

  "ensure only started jobs can be completed" in {
    Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload(Map())).complete(queue) must throwA[RuntimeException]
    Job.create(AggregateId("1"), Binding(List(BoundQueue(queue))), Payload(Map())).trigger().complete(queue) must throwA[RuntimeException]
  }

  private def typesOf(events: List[Event]) = {
    events.map(_.getClass)
  }
}
