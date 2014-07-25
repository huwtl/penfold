package com.qmetric.penfold.command

import com.qmetric.penfold.domain.store.DomainRepository
import com.qmetric.penfold.domain.model.Task

case class UnassignTaskHandler(eventStore: DomainRepository) extends CommandHandler[UnassignTask] {
  override def handle(command: UnassignTask) = {
    val unassignedTask = eventStore.getById[Task](command.id).unassign(command.version, command.unassignType, command.payloadUpdate)
    eventStore.add(unassignedTask)
    unassignedTask.aggregateId
  }
}
