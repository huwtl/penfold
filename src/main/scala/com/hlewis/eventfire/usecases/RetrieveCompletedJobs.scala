package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.{Job, JobStore}

class RetrieveCompletedJobs(jobStore: JobStore) {
  def retrieve(): Iterable[Job] = {
    jobStore.retrieve("completed")
  }
}
