package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.ScheduledJob
import org.huwtl.penfold.domain.store.DomainRepository

case class CompleteJobHandler(eventStore: DomainRepository) extends CommandHandler[CompleteJob] {
  override def handle(command: CompleteJob) {
    val job = eventStore.getById[ScheduledJob](command.id).get
    val completedJob = job.complete()
    eventStore.add(completedJob)
  }
}
