package org.huwtl.penfold.usecases

import org.huwtl.penfold.domain.{Status, Job, CompleteJobRequest, JobStore}
import org.huwtl.penfold.domain.exceptions.JobNotFoundException

class CompleteJob(jobStore: JobStore) {
  def complete(completeJobRequest: CompleteJobRequest): Job = {
    jobStore.retrieveBy(completeJobRequest.jobId) match {
      case Some(job) => {
        jobStore.updateStatus(job, Status.Completed)
      }
      case _ => throw JobNotFoundException(s"Job ${completeJobRequest.jobId} not found")
    }
  }
}
