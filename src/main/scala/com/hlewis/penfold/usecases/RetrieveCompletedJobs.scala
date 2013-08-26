package com.hlewis.penfold.usecases

import com.hlewis.penfold.domain.{Status, Job, JobStore}

class RetrieveCompletedJobs(jobStore: JobStore) {
  def retrieve(): Iterable[Job] = {
    jobStore.retrieve(Status.Completed)
  }
}
