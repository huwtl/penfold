package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.{Status, JobStore}

class RetrieveTriggeredJobsByType(jobStore: JobStore) {
  def retrieve(jobType: String) = {
    jobStore.retrieve(Status.Triggered).filter(_.jobType == jobType)
  }
}
