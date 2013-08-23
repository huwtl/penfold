package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.{Status, JobStore}

class RetrieveTriggeredJobs(jobStore: JobStore) {
  def retrieve() = {
    jobStore.retrieve(Status.Triggered)
  }
}
