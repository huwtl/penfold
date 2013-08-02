package com.hlewis.eventfire.usecases

import org.scalatest.FunSpec
import org.scalatest.mock.MockitoSugar
import com.hlewis.eventfire.domain.{Payload, Job, CompleteJobRequest, JobStore}
import org.mockito.Mockito._
import com.hlewis.eventfire.domain.exceptions.JobNotFoundException
import org.scalatest.matchers.ShouldMatchers
import org.joda.time.DateTime

class CompleteJobTest extends FunSpec with MockitoSugar with ShouldMatchers {
  val job = Job("1", "type", None, Some(new DateTime(2013, 7, 30, 0, 0, 0)), "waiting", Payload(Map()))

  val request = new CompleteJobRequest("1")

  val jobStore = mock[JobStore]

  val completeJob = new CompleteJob(jobStore)

  describe("Complete job use case") {
    it("should throw exception when job not found") {
      when(jobStore.retrieveBy("1")).thenReturn(None)

      evaluating {
        completeJob.complete(request)
      } should produce[JobNotFoundException]
    }

    it("complete job") {
      when(jobStore.retrieveBy(request.jobId)).thenReturn(Some(job))

      completeJob.complete(request)

      verify(jobStore).update(Job(job.id, job.jobType, job.cron, job.triggerDate, "completed", job.payload))
    }
  }
}
