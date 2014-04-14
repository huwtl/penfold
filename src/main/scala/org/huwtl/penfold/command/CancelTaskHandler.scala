package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.Task
import org.huwtl.penfold.domain.store.DomainRepository

case class CancelTaskHandler(eventStore: DomainRepository) extends CommandHandler[CancelTask] {
  override def handle(command: CancelTask) = {
    val task = eventStore.getById[Task](command.id)
    val cancelledTask = task.cancel(command.queueId)
    eventStore.add(cancelledTask)
    cancelledTask.aggregateId
  }
}
