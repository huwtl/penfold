package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.ScheduledJob
import org.huwtl.penfold.domain.store.{DomainRepository, EventStore}

case class CancelJobHandler(eventStore: DomainRepository) extends CommandHandler[CancelJob] {
  override def handle(command: CancelJob) {
    val job = eventStore.getById[ScheduledJob](command.id).get
    val cancelledJob = job.cancel()
    eventStore.add(cancelledJob)
  }
}
