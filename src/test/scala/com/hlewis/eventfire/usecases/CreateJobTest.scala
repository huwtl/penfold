package com.hlewis.eventfire.usecases

import org.scalatest.FunSpec
import org.scalatest.mock.MockitoSugar
import com.hlewis.eventfire.domain.{Job, JobStore}
import org.mockito.Mockito._

class CreateJobTest extends FunSpec with MockitoSugar {
  val job = mock[Job]

  val jobStore = mock[JobStore]

  val createJob = new CreateJob(jobStore)

  describe("Create job use case") {
    it("should create job") {
      createJob.create(job)

      verify(jobStore).add(job)
    }
  }
}
