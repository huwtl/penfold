package org.huwtl.penfold.usecases

import org.huwtl.penfold.domain.{Status, StartJobRequest, Job, JobStore}
import org.huwtl.penfold.domain.exceptions.JobNotFoundException

class StartJob(jobStore: JobStore) {
  def start(startJobRequest: StartJobRequest): Job = {
    jobStore.retrieveBy(startJobRequest.jobId) match {
      case Some(job) => {
        jobStore.update(Job(job.id, job.jobType, job.cron, job.triggerDate, Status.Started, job.payload))
      }
      case _ => throw JobNotFoundException(s"Job ${startJobRequest.jobId} not found")
    }
  }
}
