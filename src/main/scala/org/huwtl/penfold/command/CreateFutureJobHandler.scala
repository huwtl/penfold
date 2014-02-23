package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.Job
import org.huwtl.penfold.domain.store.{DomainRepository, EventStore}

case class CreateFutureJobHandler(eventStore: DomainRepository) extends CommandHandler[CreateFutureJob] {
  override def handle(command: CreateFutureJob) {
    val job = Job.create(command.id, command.jobType, command.triggerDate, command.payload)
    eventStore.add(job)
  }
}
