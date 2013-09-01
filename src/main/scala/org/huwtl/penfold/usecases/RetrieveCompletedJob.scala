package org.huwtl.penfold.usecases

import org.huwtl.penfold.domain.{Id, Status, Job, JobStore}

class RetrieveCompletedJob(jobStore: JobStore) {
  def retrieve(id: Id): Option[Job] = {
    jobStore.retrieveBy(id) match {
      case Some(job) if job.status == Status.Completed => Some(job)
      case _ => None
    }
  }
}
