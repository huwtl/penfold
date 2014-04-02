package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.Job
import org.huwtl.penfold.domain.store.DomainRepository
import org.huwtl.penfold.command.support.AggregateIdFactory

case class CreateFutureJobHandler(eventStore: DomainRepository, idFactory: AggregateIdFactory) extends CommandHandler[CreateFutureJob] {
  override def handle(command: CreateFutureJob) = {
    val job = Job.create(idFactory.create, command.binding, command.triggerDate, command.payload)
    eventStore.add(job)
    job.aggregateId
  }
}
