package org.huwtl.penfold.usecases

import org.huwtl.penfold.domain.{Status, JobStore}

class RetrieveTriggeredJobs(jobStore: JobStore) {
  def retrieve() = {
    jobStore.retrieve(Status.Triggered)
  }
}
