package org.huwtl.penfold.usecases

import org.huwtl.penfold.domain.JobStore

class RetrieveJobById(jobStore: JobStore) {
  def retrieve(id: String) = {
    jobStore.retrieveBy(id)
  }
}
