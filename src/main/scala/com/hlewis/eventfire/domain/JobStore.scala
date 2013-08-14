package com.hlewis.eventfire.domain

trait JobStore {
  def add(job: Job): Job

  def update(job: Job) : Job

  def remove(job: Job)

  def triggerPendingJobs()

  def retrieveBy(id: String): Option[Job]

  def retrieve(status: String): Iterable[Job]
}
