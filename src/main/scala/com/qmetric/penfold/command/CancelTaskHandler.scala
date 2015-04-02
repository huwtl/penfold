package com.qmetric.penfold.command

import com.qmetric.penfold.domain.model.Task
import com.qmetric.penfold.domain.store.DomainRepository

case class CancelTaskHandler(eventStore: DomainRepository) extends CommandHandler[CancelTask] {
  override def handle(command: CancelTask) = {
    val cancelledTask = eventStore.getById[Task](command.id).cancel(command.version, command.user, command.reason, command.payloadUpdate)
    eventStore.add(cancelledTask)
    cancelledTask.aggregateId
  }
}
