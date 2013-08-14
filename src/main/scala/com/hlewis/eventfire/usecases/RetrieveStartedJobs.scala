package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.JobStore

class RetrieveStartedJobs(jobStore: JobStore) {
  def retrieve() = {
    jobStore.retrieve("started")
  }
}
