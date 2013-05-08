package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.{Job, JobStore}

class CancelExistingJob(jobStore: JobStore) {
  def cancel(job: Job) {
    jobStore.remove(job)
  }
}
