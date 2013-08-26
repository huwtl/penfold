package com.hlewis.penfold.usecases

import com.hlewis.penfold.domain.{Status, Job, CompleteJobRequest, JobStore}
import com.hlewis.penfold.domain.exceptions.JobNotFoundException

class CompleteJob(jobStore: JobStore) {
  def complete(completeJobRequest: CompleteJobRequest): Job = {
    jobStore.retrieveBy(completeJobRequest.jobId) match {
      case Some(job) => {
        jobStore.update(Job(job.id, job.jobType, job.cron, job.triggerDate, Status.Completed, job.payload))
      }
      case _ => throw JobNotFoundException(s"Job ${completeJobRequest.jobId} not found")
    }
  }
}
