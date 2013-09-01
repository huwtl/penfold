package org.huwtl.penfold.usecases

import org.huwtl.penfold.domain.{Id, Status, JobStore}

class RetrieveTriggeredJob(jobStore: JobStore) {
  def retrieveBy(id: Id) = {
    jobStore.retrieveBy(id) match {
      case Some(job) if job.status == Status.Triggered => Some(job)
      case _ => None
    }
  }
}
