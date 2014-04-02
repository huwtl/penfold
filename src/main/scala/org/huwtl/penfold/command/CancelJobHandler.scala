package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.Job
import org.huwtl.penfold.domain.store.DomainRepository

case class CancelJobHandler(eventStore: DomainRepository) extends CommandHandler[CancelJob] {
  override def handle(command: CancelJob) = {
    val job = eventStore.getById[Job](command.id).get
    val cancelledJob = job.cancel(command.queueId)
    eventStore.add(cancelledJob)
    cancelledJob.aggregateId
  }
}
