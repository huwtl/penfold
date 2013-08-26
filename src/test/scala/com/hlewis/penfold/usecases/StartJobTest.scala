package com.hlewis.penfold.usecases

import com.hlewis.penfold.domain._
import com.hlewis.penfold.domain.StartJobRequest
import scala.Some
import com.hlewis.penfold.domain.Job
import org.joda.time.DateTime
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import com.hlewis.penfold.domain.exceptions.JobNotFoundException

class StartJobTest extends Specification with Mockito {

  trait context extends Scope {
    val job = Job("1", "type", None, Some(new DateTime(2013, 7, 30, 0, 0, 0)), Status.Waiting, Payload(Map()))

    val jobStore = mock[JobStore]

    val request = new StartJobRequest("1")

    val startJob = new StartJob(jobStore)
  }

  "start job" in new context {
    jobStore.retrieveBy("1") returns Some(job)

    startJob.start(request)

    there was one(jobStore).update(Job(job.id, job.jobType, job.cron, job.triggerDate, Status.Started, job.payload))
  }

  "throw exception when job not found" in new context {
    jobStore.retrieveBy("1") returns None

    startJob.start(request) must throwA[JobNotFoundException]
  }
}
