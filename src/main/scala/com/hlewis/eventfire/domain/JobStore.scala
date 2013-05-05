package com.hlewis.eventfire.domain

trait JobStore {
  def add(job: Job): Boolean
}
