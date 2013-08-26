package org.huwtl.penfold.usecases

import org.huwtl.penfold.domain.{Job, JobStore}

class CreateJob(jobStore: JobStore) {
  def create(job: Job) = {
    jobStore.add(job)
  }
}
