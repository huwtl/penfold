package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.Task
import org.huwtl.penfold.domain.store.DomainRepository

case class ArchiveTaskHandler(eventStore: DomainRepository) extends CommandHandler[ArchiveTask] {
  override def handle(command: ArchiveTask) = {
    val archivedTask = eventStore.getById[Task](command.id).archive()
    eventStore.add(archivedTask)
    archivedTask.aggregateId
  }
}
