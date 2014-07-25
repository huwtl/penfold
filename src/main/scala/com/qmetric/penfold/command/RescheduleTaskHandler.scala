package com.qmetric.penfold.command

import com.qmetric.penfold.domain.store.DomainRepository
import com.qmetric.penfold.domain.model.Task

case class RescheduleTaskHandler(eventStore: DomainRepository) extends CommandHandler[RescheduleTask] {
  override def handle(command: RescheduleTask) = {
    val rescheduledTask = eventStore.getById[Task](command.id).reschedule(command.version, command.triggerDate, command.assignee, command.rescheduleType, command.payloadUpdate, command.scoreUpdate)
    eventStore.add(rescheduledTask)
    rescheduledTask.aggregateId
  }
}
