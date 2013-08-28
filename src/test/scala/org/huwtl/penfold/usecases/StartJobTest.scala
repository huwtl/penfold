package org.huwtl.penfold.usecases

import org.huwtl.penfold.domain._
import org.huwtl.penfold.domain.StartJobRequest
import scala.Some
import org.huwtl.penfold.domain.Job
import org.joda.time.DateTime
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import org.huwtl.penfold.domain.exceptions.JobNotFoundException

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

    there was one(jobStore).updateStatus(job, Status.Started)
  }

  "throw exception when job not found" in new context {
    jobStore.retrieveBy("1") returns None

    startJob.start(request) must throwA[JobNotFoundException]
  }
}
