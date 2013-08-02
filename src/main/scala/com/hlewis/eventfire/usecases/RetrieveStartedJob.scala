package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.{Job, JobStore}

class RetrieveStartedJob(jobStore: JobStore) {

  def retrieve(id: String): Option[Job] = {
    jobStore.retrieveBy(id) match {
      case Some(job) if job.status == "started" => Some(job)
      case _ => None
    }
  }
}
