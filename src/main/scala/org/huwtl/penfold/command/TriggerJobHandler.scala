package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.ScheduledJob
import org.huwtl.penfold.domain.store.{DomainRepository, EventStore}

case class TriggerJobHandler(eventStore: DomainRepository) extends CommandHandler[TriggerJob] {
  override def handle(command: TriggerJob) {
    val job = eventStore.getById[ScheduledJob](command.id).get
    val triggeredJob = job.trigger()
    eventStore.add(triggeredJob)
  }
}
