package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.{StartJobRequest, Job, JobStore}
import com.hlewis.eventfire.domain.exceptions.JobNotFoundException

class StartJob(jobStore: JobStore) {
  def start(startJobRequest: StartJobRequest): Job = {
    jobStore.retrieveBy(startJobRequest.jobId) match {
      case Some(job) => {
        jobStore.update(Job(job.id, job.jobType, job.cron, job.triggerDate, "started", job.payload))
      }
      case _ => throw JobNotFoundException(s"Job ${startJobRequest.jobId} not found")
    }
  }
}
