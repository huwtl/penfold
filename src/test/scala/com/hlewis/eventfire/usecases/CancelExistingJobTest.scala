package com.hlewis.eventfire.usecases

import org.scalatest.FunSpec
import org.scalatest.mock.MockitoSugar
import com.hlewis.eventfire.domain.{Job, JobStore}
import org.mockito.Mockito._

class CancelExistingJobTest extends FunSpec with MockitoSugar {
  val jobStore = mock[JobStore]

  val cancelExistingJob = new CancelExistingJob(jobStore)

  describe("Cancel existing job use case") {
    it("should cancel existing job") {
      val job = mock[Job]

      cancelExistingJob.cancel(job)

      verify(jobStore).remove(job)
    }
  }
}
