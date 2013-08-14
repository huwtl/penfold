package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.{Job, JobStore}

class CreateJob(jobStore: JobStore) {
  def create(job: Job) = {
    jobStore.add(job)
  }
}
