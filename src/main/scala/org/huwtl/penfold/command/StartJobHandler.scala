package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.ScheduledJob
import org.huwtl.penfold.domain.store.{DomainRepository, EventStore}

case class StartJobHandler(eventStore: DomainRepository) extends CommandHandler[StartJob] {
  override def handle(command: StartJob) {
    val job = eventStore.getById[ScheduledJob](command.id).get
    val startedJob: ScheduledJob = job.start()
    eventStore.add(startedJob)
  }
}
