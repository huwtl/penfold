package com.hlewis.rabbit_scheduler.domain

trait JobExchange {
  def receive(job: Job)

  def dispatch(job: Job)
}
