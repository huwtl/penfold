package com.qmetric.penfold.command

import com.qmetric.penfold.domain.model.Task
import com.qmetric.penfold.domain.store.DomainRepository

case class TriggerTaskHandler(eventStore: DomainRepository) extends CommandHandler[TriggerTask] {
  override def handle(command: TriggerTask) = {
    val readyTask = eventStore.getById[Task](command.id).trigger
    eventStore.add(readyTask)
    readyTask.aggregateId
  }
}
