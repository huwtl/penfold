package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.{Status, JobStore}

class RetrieveStartedJobs(jobStore: JobStore) {
  def retrieve() = {
    jobStore.retrieve(Status.Started)
  }
}
