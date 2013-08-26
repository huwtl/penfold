package org.huwtl.penfold.usecases

import org.huwtl.penfold.domain.{Status, Job, JobStore}

class RetrieveCompletedJobs(jobStore: JobStore) {
  def retrieve(): Iterable[Job] = {
    jobStore.retrieve(Status.Completed)
  }
}
