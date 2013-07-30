package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.JobStore

class RetrieveTriggeredJob(jobStore: JobStore) {
  def retrieve(id: String) = {
    // todo should be None when exists but not in triggered state
    jobStore.retrieve(id)
  }
}
