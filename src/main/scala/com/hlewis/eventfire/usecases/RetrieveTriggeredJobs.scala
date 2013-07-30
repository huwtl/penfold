package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.JobStore

class RetrieveTriggeredJobs(jobStore: JobStore) {
  def retrieve() = {
    jobStore.retrieveTriggered()
  }
}
