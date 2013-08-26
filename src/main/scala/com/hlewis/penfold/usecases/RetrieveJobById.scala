package com.hlewis.penfold.usecases

import com.hlewis.penfold.domain.JobStore

class RetrieveJobById(jobStore: JobStore) {
  def retrieve(id: String) = {
    jobStore.retrieveBy(id)
  }
}
