package com.hlewis.eventfire.domain

trait JobExchange {
  def receive(job: Job)

  def dispatch(job: Job)
}
