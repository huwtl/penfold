package com.hlewis.penfold.usecases

import com.hlewis.penfold.domain.{Status, JobStore}

class RetrieveTriggeredJobs(jobStore: JobStore) {
  def retrieve() = {
    jobStore.retrieve(Status.Triggered)
  }
}
