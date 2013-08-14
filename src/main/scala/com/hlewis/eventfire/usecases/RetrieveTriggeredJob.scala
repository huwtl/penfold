package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.JobStore

class RetrieveTriggeredJob(jobStore: JobStore) {
  def retrieveBy(id: String) = {
    jobStore.retrieveBy(id) match {
      case Some(job) if job.status == "triggered" => Some(job)
      case _ => None
    }
  }
}
