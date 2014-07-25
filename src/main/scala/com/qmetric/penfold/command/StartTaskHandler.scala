package com.qmetric.penfold.command

import com.qmetric.penfold.domain.model.Task
import com.qmetric.penfold.domain.store.DomainRepository

case class StartTaskHandler(eventStore: DomainRepository) extends CommandHandler[StartTask] {
  override def handle(command: StartTask) = {
    val startedTask = eventStore.getById[Task](command.id).start(command.version, command.assignee, command.payloadUpdate)
    eventStore.add(startedTask)
    startedTask.aggregateId
  }
}
