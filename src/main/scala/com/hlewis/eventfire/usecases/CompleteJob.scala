package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.{Job, CompleteJobRequest, JobStore}
import com.hlewis.eventfire.domain.exceptions.JobNotFoundException

class CompleteJob(jobStore: JobStore) {
  def complete(completeJobRequest: CompleteJobRequest): Job = {
    jobStore.retrieveBy(completeJobRequest.jobId) match {
      case Some(job) => {
        jobStore.update(Job(job.id, job.jobType, job.cron, job.triggerDate, "completed", job.payload))
      }
      case _ => throw JobNotFoundException(s"Job ${completeJobRequest.jobId} not found")
    }
  }
}
