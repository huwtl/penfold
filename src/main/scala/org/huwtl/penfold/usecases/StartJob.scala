package org.huwtl.penfold.usecases

import org.huwtl.penfold.domain.{Status, StartJobRequest, Job, JobStore}
import org.huwtl.penfold.domain.exceptions.JobNotFoundException

class StartJob(jobStore: JobStore) {
  def start(startJobRequest: StartJobRequest): Job = {
    jobStore.retrieveBy(startJobRequest.jobId) match {
      case Some(job) => {
        jobStore.updateStatus(job, Status.Started)
      }
      case _ => throw JobNotFoundException(s"Job ${startJobRequest.jobId} not found")
    }
  }
}
