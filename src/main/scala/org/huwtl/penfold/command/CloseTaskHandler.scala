package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.Task
import org.huwtl.penfold.domain.store.DomainRepository

case class CloseTaskHandler(eventStore: DomainRepository) extends CommandHandler[CloseTask] {
  override def handle(command: CloseTask) = {
    val closeTask = eventStore.getById[Task](command.id).close(command.version, command.concluder, command.closeReason, command.assignee, command.payloadUpdate)
    eventStore.add(closeTask)
    closeTask.aggregateId
  }
}
