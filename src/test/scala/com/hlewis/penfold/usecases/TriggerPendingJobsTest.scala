package com.hlewis.penfold.usecases

import com.hlewis.penfold.domain.JobStore
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito

class TriggerPendingJobsTest extends Specification with Mockito {
  val jobStore = mock[JobStore]

  val triggerPendingJobs = new TriggerPendingJobs(jobStore)

  "trigger jobs with trigger date in past" in {
    triggerPendingJobs.triggerPending()

    there was one(jobStore).triggerPendingJobs()
  }
}
