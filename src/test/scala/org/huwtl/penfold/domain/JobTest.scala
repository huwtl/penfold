package org.huwtl.penfold.domain

import org.joda.time.DateTime
import org.joda.time.DateTime.now
import org.specs2.mutable.Specification

class JobTest extends Specification {
  val triggerDate = new DateTime(2013, 7, 28, 12, 30, 0)

  "construct new job with trigger date calculated from cron" in {
    Job(Id(""), JobType(""), Some(Cron("0 30 12 28 07 * 2013")), None, Status.Waiting, Payload(Map())).nextTriggerDate must beEqualTo(triggerDate)
  }

  "construct new job with explicit trigger date" in {
    Job(Id(""), JobType(""), None, Some(triggerDate), Status.Waiting, Payload(Map())).nextTriggerDate must beEqualTo(triggerDate)
    Job(Id(""), JobType(""), Some(Cron("1 30 12 28 07 * 2013")), Some(triggerDate), Status.Waiting, Payload(Map())).nextTriggerDate must beEqualTo(triggerDate)
  }

  "construct new job with default trigger date" in {
    Job(Id(""), JobType(""), None, None, Status.Waiting, Payload(Map())).nextTriggerDate must not beNull
  }

  "construct new job with created and lastModified date specified" in {
    val created = now
    val lastModified = now
    val job = Job(id = Id(""), jobType = JobType(""), status = Status.Waiting, payload = Payload(Map()), created = Some(created), lastModified = Some(lastModified))
    job.created.get must beEqualTo(created)
    job.created.get must beEqualTo(lastModified)
  }
}
