package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.JobStore

class RetrieveJob(jobStore: JobStore) {

  def retrieve(id: String) = {
    jobStore.retrieve(id)
  }
}
