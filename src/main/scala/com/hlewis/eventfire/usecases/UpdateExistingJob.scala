package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.{Job, JobStore}

class UpdateExistingJob(jobStore: JobStore) {
  def update(job: Job) {
    jobStore.update(job)
  }
}
