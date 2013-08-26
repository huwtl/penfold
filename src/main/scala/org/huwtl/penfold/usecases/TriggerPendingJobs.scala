package org.huwtl.penfold.usecases

import org.huwtl.penfold.domain.JobStore

class TriggerPendingJobs(jobStore: JobStore) {
  def triggerPending() {
    jobStore.triggerPendingJobs()
  }
}
