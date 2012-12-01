package com.hlewis.rabbit_scheduler.job

trait JobConsumer {
  def consume(job: Job)
}
