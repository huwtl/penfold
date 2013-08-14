package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.JobStore

class TriggerPendingJobs(jobStore: JobStore) {
  def triggerPending() {
    jobStore.triggerPendingJobs()
  }
}
