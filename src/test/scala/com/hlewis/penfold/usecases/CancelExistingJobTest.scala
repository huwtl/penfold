package com.hlewis.penfold.usecases

import com.hlewis.penfold.domain.{Job, JobStore}
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
