package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.{Job, JobStore}

class RetrieveCompletedJob(jobStore: JobStore) {
  def retrieve(id: String): Option[Job] = {
    jobStore.retrieveBy(id) match {
      case Some(job) if job.status == "completed" => Some(job)
      case _ => None
    }
  }
}
