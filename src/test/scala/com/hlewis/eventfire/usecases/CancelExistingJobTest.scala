package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.{Job, JobStore}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class CancelExistingJobTest extends Specification with Mockito {
  val jobStore = mock[JobStore]

  val cancelExistingJob = new CancelExistingJob(jobStore)

  "cancel existing job" in {
    val job = mock[Job]

    cancelExistingJob.cancel(job)

    there was one(jobStore).remove(job)
  }
}
