package com.hlewis.penfold.usecases

import com.hlewis.penfold.domain.{Job, JobStore}

class CreateJob(jobStore: JobStore) {
  def create(job: Job) = {
    jobStore.add(job)
  }
}
