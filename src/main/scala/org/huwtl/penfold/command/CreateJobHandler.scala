package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.Job
import org.huwtl.penfold.domain.store.{DomainRepository, EventStore}

case class CreateJobHandler(eventStore: DomainRepository) extends CommandHandler[CreateJob] {
  override def handle(command: CreateJob) {
    val job = Job.create(command.id, command.jobType, command.payload)
    eventStore.add(job)
  }
}
