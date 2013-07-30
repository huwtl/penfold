package com.hlewis.eventfire.domain

trait JobStore {

  def retrieveTriggered(): Iterable[Job]

  def retrieveTriggered(jobType: String): Iterable[Job]

  def retrieveStarted(): Iterable[Job]

  def retrieveCompleted(): Iterable[Job]

  def retrieve(id: String): Option[Job]

  def add(job: Job): Job

  def update(job: Job) : Job

  def remove(job: Job)
}
