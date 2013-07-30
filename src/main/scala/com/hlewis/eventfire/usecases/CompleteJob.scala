package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.{Job, CompleteJobRequest, JobStore}

class CompleteJob(jobStore: JobStore) {
  def complete(completeJobRequest: CompleteJobRequest): Option[Job] = {
    jobStore.retrieve(completeJobRequest.jobId) match {
      case Some(job) => {
        jobStore.remove(job)
        Some(job)
      }
      case _ => None
    }
  }
}
