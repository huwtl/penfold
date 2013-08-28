package org.huwtl.penfold.usecases

import org.huwtl.penfold.domain._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.Payload
import scala.Some
import org.huwtl.penfold.domain.Job
import org.huwtl.penfold.domain.exceptions.JobNotFoundException
import org.huwtl.penfold.domain.CompleteJobRequest
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class CompleteJobTest extends Specification with Mockito {

  trait context extends Scope {
    val job = Job("1", "type", None, Some(new DateTime(2013, 7, 30, 0, 0, 0)), Status.Waiting, Payload(Map()))

    val request = new CompleteJobRequest("1")

    val jobStore = mock[JobStore]

    val completeJob = new CompleteJob(jobStore)
  }

  "complete job" in new context {
    jobStore.retrieveBy(request.jobId) returns Some(job)

    completeJob.complete(request)

    there was one(jobStore).updateStatus(job, Status.Completed)
  }

  "throw exception when job not found" in new context {
    jobStore.retrieveBy("1") returns None

    completeJob.complete(request) must throwA[JobNotFoundException]
  }
}
