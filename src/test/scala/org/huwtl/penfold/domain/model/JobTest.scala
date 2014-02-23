package org.huwtl.penfold.domain.model

import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.event._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.domain.event.JobTriggered
import org.huwtl.penfold.domain.event.JobStarted

class JobTest extends Specification {

  "create new job" in {
    val createdJob = Job.create(Id("1"), JobType(""), Payload(Map()))
    typesOf(createdJob.uncommittedEvents) must beEqualTo(List(classOf[JobCreated]))
  }

  "create new future job" in {
    val createdJob = Job.create(Id("1"), JobType(""), DateTime.now(), Payload(Map()))
    typesOf(createdJob.uncommittedEvents) must beEqualTo(List(classOf[JobCreated]))
  }

  "trigger job" in {
    val triggeredJob = Job.create(Id("1"), JobType(""), Payload(Map())).trigger()
    typesOf(triggeredJob.uncommittedEvents) must beEqualTo(List(classOf[JobTriggered], classOf[JobCreated]))
  }

  "ensure only waiting jobs can be triggered" in {
    Job.create(Id("1"), JobType(""), Payload(Map())).trigger().trigger() must throwA[RuntimeException]
    Job.create(Id("1"), JobType(""), Payload(Map())).trigger().start().trigger() must throwA[RuntimeException]
  }

  "start job" in {
    val startedJob = Job.create(Id("1"), JobType(""), Payload(Map())).trigger().start()
    typesOf(startedJob.uncommittedEvents) must beEqualTo(List(classOf[JobStarted], classOf[JobTriggered], classOf[JobCreated]))
  }

  "ensure only triggered jobs can be started" in {
    Job.create(Id("1"), JobType(""), Payload(Map())).start() must throwA[RuntimeException]
    Job.create(Id("1"), JobType(""), Payload(Map())).trigger().start().start() must throwA[RuntimeException]
  }

  "cancel job" in {
    val startedJob = Job.create(Id("1"), JobType(""), Payload(Map())).trigger().cancel()
    typesOf(startedJob.uncommittedEvents) must beEqualTo(List(classOf[JobCancelled], classOf[JobTriggered], classOf[JobCreated]))
  }

  "complete job" in {
    val startedJob = Job.create(Id("1"), JobType(""), Payload(Map())).trigger().start().complete()
    typesOf(startedJob.uncommittedEvents) must beEqualTo(List(classOf[JobCompleted], classOf[JobStarted], classOf[JobTriggered], classOf[JobCreated]))
  }

  "ensure only started jobs can be completed" in {
    Job.create(Id("1"), JobType(""), Payload(Map())).complete() must throwA[RuntimeException]
    Job.create(Id("1"), JobType(""), Payload(Map())).trigger().complete() must throwA[RuntimeException]
  }

  private def typesOf(events: List[Event]) = {
    events.map(_.getClass)
  }
}
