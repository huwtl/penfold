package com.hlewis.penfold.usecases

import com.hlewis.penfold.domain.{Status, JobStore}

class RetrieveTriggeredJobsByType(jobStore: JobStore) {
  def retrieve(jobType: String) = {
    jobStore.retrieve(Status.Triggered).filter(_.jobType == jobType)
  }
}
