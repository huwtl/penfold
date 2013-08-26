package com.hlewis.penfold.usecases

import com.hlewis.penfold.domain.{Status, Job, JobStore}

class RetrieveStartedJob(jobStore: JobStore) {
  def retrieve(id: String): Option[Job] = {
    jobStore.retrieveBy(id) match {
      case Some(job) if job.status == Status.Started => Some(job)
      case _ => None
    }
  }
}
