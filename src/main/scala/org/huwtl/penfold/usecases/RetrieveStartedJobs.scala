package org.huwtl.penfold.usecases

import org.huwtl.penfold.domain.{Status, JobStore}

class RetrieveStartedJobs(jobStore: JobStore) {
  def retrieve() = {
    jobStore.retrieve(Status.Started)
  }
}
