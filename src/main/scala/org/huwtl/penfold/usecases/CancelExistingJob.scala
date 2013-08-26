package org.huwtl.penfold.usecases

import org.huwtl.penfold.domain.{Job, JobStore}

class CancelExistingJob(jobStore: JobStore) {
  def cancel(job: Job) {
    jobStore.remove(job)
  }
}
