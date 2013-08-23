package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.{Status, JobStore}

class RetrieveTriggeredJob(jobStore: JobStore) {
  def retrieveBy(id: String) = {
    jobStore.retrieveBy(id) match {
      case Some(job) if job.status == Status.Triggered => Some(job)
      case _ => None
    }
  }
}
