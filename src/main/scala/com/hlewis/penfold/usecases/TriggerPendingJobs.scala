package com.hlewis.penfold.usecases

import com.hlewis.penfold.domain.JobStore

class TriggerPendingJobs(jobStore: JobStore) {
  def triggerPending() {
    jobStore.triggerPendingJobs()
  }
}
