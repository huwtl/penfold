package com.hlewis.penfold.usecases

import com.hlewis.penfold.domain.{Status, JobStore}

class RetrieveStartedJobs(jobStore: JobStore) {
  def retrieve() = {
    jobStore.retrieve(Status.Started)
  }
}
