package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.Job
import org.huwtl.penfold.domain.store.DomainRepository

case class StartJobHandler(eventStore: DomainRepository) extends CommandHandler[StartJob] {
  override def handle(command: StartJob) = {
    val job = eventStore.getById[Job](command.id).get
    val startedJob = job.start(command.queueId)
    eventStore.add(startedJob)
    startedJob.aggregateId
  }
}
