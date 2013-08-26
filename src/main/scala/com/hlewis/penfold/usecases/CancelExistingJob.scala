package com.hlewis.penfold.usecases

import com.hlewis.penfold.domain.{Job, JobStore}

class CancelExistingJob(jobStore: JobStore) {
  def cancel(job: Job) {
    jobStore.remove(job)
  }
}
