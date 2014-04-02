package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.Job
import org.huwtl.penfold.domain.store.DomainRepository

case class CompleteJobHandler(eventStore: DomainRepository) extends CommandHandler[CompleteJob] {
  override def handle(command: CompleteJob) = {
    val job = eventStore.getById[Job](command.id)
    val completedJob = job.complete(command.queueId)
    eventStore.add(completedJob)
    completedJob.aggregateId
  }
}
