package com.qmetric.penfold.command

import com.qmetric.penfold.domain.model.Task
import com.qmetric.penfold.domain.store.DomainRepository

case class ArchiveTaskHandler(eventStore: DomainRepository) extends CommandHandler[ArchiveTask] {
  override def handle(command: ArchiveTask) = {
    val archivedTask = eventStore.getById[Task](command.id).archive(command.version)
    eventStore.add(archivedTask)
    archivedTask.aggregateId
  }
}
