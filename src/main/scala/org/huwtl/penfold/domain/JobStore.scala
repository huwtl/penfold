package org.huwtl.penfold.domain

import org.huwtl.penfold.domain.exceptions.JobUpdateConflictException

trait JobStore {
  def add(job: Job): Job

  @throws(classOf[JobUpdateConflictException])
  def updateStatus(job: Job, status: Status) : Job

  def remove(job: Job)

  def triggerPendingJobs()

  def retrieveBy(id: Id): Option[Job]

  def retrieve(status: Status): Iterable[Job]
}
