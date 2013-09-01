package org.huwtl.penfold.usecases

import org.huwtl.penfold.domain.{Id, JobStore}

class RetrieveJobById(jobStore: JobStore) {
  def retrieve(id: Id) = {
    jobStore.retrieveBy(id)
  }
}
