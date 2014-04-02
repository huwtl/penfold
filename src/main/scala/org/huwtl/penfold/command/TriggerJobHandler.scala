package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.Job
import org.huwtl.penfold.domain.store.DomainRepository

case class TriggerJobHandler(eventStore: DomainRepository) extends CommandHandler[TriggerJob] {
  override def handle(command: TriggerJob) = {
    val job = eventStore.getById[Job](command.id)
    val readyJob = job.trigger()
    eventStore.add(readyJob)
    readyJob.aggregateId
  }
}
