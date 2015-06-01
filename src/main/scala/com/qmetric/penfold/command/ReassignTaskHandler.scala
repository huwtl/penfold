package com.qmetric.penfold.command

import com.qmetric.penfold.domain.model.Task
import com.qmetric.penfold.domain.store.DomainRepository

case class ReassignTaskHandler(eventStore: DomainRepository) extends CommandHandler[ReassignTask] {
  override def handle(command: ReassignTask) = {
    val unassignedTask = eventStore.getById[Task](command.id).reassign(command.version, command.assignee, command.reassignType, command.payloadUpdate)
    eventStore.add(unassignedTask)
    unassignedTask.aggregateId
  }
}
