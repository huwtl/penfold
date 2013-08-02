package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.JobStore

class RetrieveJobById(jobStore: JobStore) {
  def retrieve(id: String) = {
    jobStore.retrieveBy(id)
  }
}
