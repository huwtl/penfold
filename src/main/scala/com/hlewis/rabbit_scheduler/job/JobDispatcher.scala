package com.hlewis.rabbit_scheduler.job


trait JobDispatcher {
  def dispatch(job: Job)
}
