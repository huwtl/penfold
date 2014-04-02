package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.Job
import org.huwtl.penfold.domain.store.DomainRepository
import org.huwtl.penfold.command.support.AggregateIdFactory

case class CreateJobHandler(eventStore: DomainRepository, idFactory: AggregateIdFactory) extends CommandHandler[CreateJob] {
  override def handle(command: CreateJob) = {
    val job = Job.create(idFactory.create, command.binding, command.payload)
    eventStore.add(job)
    job.aggregateId
  }
}
