package com.hlewis.eventfire.usecases

import org.scalatest.FunSpec
import org.scalatest.mock.MockitoSugar
import com.hlewis.eventfire.domain.{Job, CompleteJobRequest, JobStore}
import org.mockito.Mockito._
import org.mockito.Matchers._

class CompleteJobTest extends FunSpec with MockitoSugar {
  val job = mock[Job]

  val request = new CompleteJobRequest("1")

  val jobStore = mock[JobStore]

  val completeJob = new CompleteJob(jobStore)

  describe("Complete job use case") {
    it("ignore completion of non existing job") {
      when(jobStore.retrieveBy(request.jobId)).thenReturn(None)

      completeJob.complete(request)

      verify(jobStore, never()).remove(any())
    }

    it("complete job") {
      when(jobStore.retrieveBy(request.jobId)).thenReturn(Some(job))

      completeJob.complete(request)

      verify(jobStore).remove(job)
    }
  }
}
