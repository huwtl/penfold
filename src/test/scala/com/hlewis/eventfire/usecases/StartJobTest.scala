package com.hlewis.eventfire.usecases

import org.scalatest.FunSpec
import org.scalatest.mock.MockitoSugar
import com.hlewis.eventfire.domain._
import org.mockito.Mockito._
import org.scalatest.matchers.ShouldMatchers
import com.hlewis.eventfire.domain.StartJobRequest
import scala.Some
import com.hlewis.eventfire.domain.Job
import org.joda.time.DateTime
import com.hlewis.eventfire.domain.exceptions.JobNotFoundException

class StartJobTest extends FunSpec with ShouldMatchers with MockitoSugar {
  val job = Job("1", "type", None, Some(new DateTime(2013, 7, 30, 0, 0, 0)), "waiting", Payload(Map()))

  val jobStore = mock[JobStore]

  val request = new StartJobRequest("1")

  val startJob = new StartJob(jobStore)

  describe("Start job use case") {
    it("should throw exception when job not found") {
      when(jobStore.retrieve("1")).thenReturn(None)

      evaluating {
        startJob.start(request)
      } should produce[JobNotFoundException]
    }

    it("should start job") {
      when(jobStore.retrieve("1")).thenReturn(Some(job))

      startJob.start(request)

      verify(jobStore).update(Job("1", "type", None, Some(new DateTime(2013, 7, 30, 0, 0, 0)), "started", Payload(Map())))
    }
  }
}
