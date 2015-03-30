package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.Task
import org.huwtl.penfold.domain.store.DomainRepository

case class CancelTaskHandler(eventStore: DomainRepository) extends CommandHandler[CancelTask] {
  override def handle(command: CancelTask) = {
    val cancelledTask = eventStore.getById[Task](command.id).cancel(command.version, command.user, command.reason, command.payloadUpdate)
    eventStore.add(cancelledTask)
    cancelledTask.aggregateId
  }
}
