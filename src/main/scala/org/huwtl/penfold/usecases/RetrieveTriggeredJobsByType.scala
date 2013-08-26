package org.huwtl.penfold.usecases

import org.huwtl.penfold.domain.{Status, JobStore}

class RetrieveTriggeredJobsByType(jobStore: JobStore) {
  def retrieve(jobType: String) = {
    jobStore.retrieve(Status.Triggered).filter(_.jobType == jobType)
  }
}
