package com.hlewis.penfold.usecases

import com.hlewis.penfold.domain.{Job, JobStore}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class CreateJobTest extends Specification with Mockito {
  val job = mock[Job]

  val jobStore = mock[JobStore]

  val createJob = new CreateJob(jobStore)

  "create job" in {
    createJob.create(job)

    there was one(jobStore).add(job)
  }
}
