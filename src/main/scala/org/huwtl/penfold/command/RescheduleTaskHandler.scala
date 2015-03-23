package org.huwtl.penfold.command

import org.huwtl.penfold.domain.store.DomainRepository
import org.huwtl.penfold.domain.model.Task

case class RescheduleTaskHandler(eventStore: DomainRepository) extends CommandHandler[RescheduleTask] {
  override def handle(command: RescheduleTask) = {
    val rescheduledTask = eventStore.getById[Task](command.id).reschedule(command.version, command.triggerDate, command.assignee, command.reason, command.payloadUpdate, command.scoreUpdate)
    eventStore.add(rescheduledTask)
    rescheduledTask.aggregateId
  }
}
