package org.huwtl.penfold.command

import org.huwtl.penfold.domain.store.DomainRepository
import org.huwtl.penfold.domain.model.Task

case class UnassignTaskHandler(eventStore: DomainRepository) extends CommandHandler[UnassignTask] {
  override def handle(command: UnassignTask) = {
    val unassignedTask = eventStore.getById[Task](command.id).unassign(command.version, command.unassignType, command.payloadUpdate)
    eventStore.add(unassignedTask)
    unassignedTask.aggregateId
  }
}
