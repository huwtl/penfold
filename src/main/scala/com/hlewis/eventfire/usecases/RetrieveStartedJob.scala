package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.{Status, Job, JobStore}

class RetrieveStartedJob(jobStore: JobStore) {
  def retrieve(id: String): Option[Job] = {
    jobStore.retrieveBy(id) match {
      case Some(job) if job.status == Status.Started => Some(job)
      case _ => None
    }
  }
}
