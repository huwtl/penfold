package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.{Status, Job, JobStore}

class RetrieveCompletedJob(jobStore: JobStore) {
  def retrieve(id: String): Option[Job] = {
    jobStore.retrieveBy(id) match {
      case Some(job) if job.status == Status.Completed => Some(job)
      case _ => None
    }
  }
}
