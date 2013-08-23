package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.{Status, Job, JobStore}

class RetrieveCompletedJobs(jobStore: JobStore) {
  def retrieve(): Iterable[Job] = {
    jobStore.retrieve(Status.Completed)
  }
}
