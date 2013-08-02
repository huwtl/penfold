package com.hlewis.eventfire.domain

trait JobStore {
  def add(job: Job): Job

  def update(job: Job) : Job

  def remove(job: Job)

  def retrieveBy(id: String): Option[Job]

  def retrieveTriggered(): Iterable[Job]

  def retrieveTriggeredWith(jobType: String): Iterable[Job]
}
