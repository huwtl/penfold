package com.qmetric.penfold.command

import com.qmetric.penfold.domain.model.Task
import com.qmetric.penfold.domain.store.DomainRepository

case class CloseTaskHandler(eventStore: DomainRepository) extends CommandHandler[CloseTask] {
  override def handle(command: CloseTask) = {
    val closeTask = eventStore.getById[Task](command.id).close(command.version, command.user, command.reason, command.resultType, command.payloadUpdate)
    eventStore.add(closeTask)
    closeTask.aggregateId
  }
}
