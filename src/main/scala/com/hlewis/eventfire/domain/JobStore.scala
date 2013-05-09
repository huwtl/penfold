package com.hlewis.eventfire.domain

trait JobStore {
  def retrievePending(): List[Job]

  def add(job: Job): Boolean

  def update(job: Job): Boolean

  def remove(job: Job): Boolean
}
