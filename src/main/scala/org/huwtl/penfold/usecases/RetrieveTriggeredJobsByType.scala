package org.huwtl.penfold.usecases

import org.huwtl.penfold.domain.{JobType, Status, JobStore}

class RetrieveTriggeredJobsByType(jobStore: JobStore) {
  def retrieve(jobType: JobType) = {
    jobStore.retrieve(Status.Triggered).filter(_.jobType == jobType)
  }
}
