package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.{Job, JobStore}

class AddNewJob(jobStore: JobStore) {
  def add(job: Job) {
    jobStore.add(job)
  }
}
