package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.Task
import org.huwtl.penfold.domain.store.DomainRepository

case class StartTaskHandler(eventStore: DomainRepository) extends CommandHandler[StartTask] {
  override def handle(command: StartTask) = {
    val startedTask = eventStore.getById[Task](command.id).start(command.assignee)
    eventStore.add(startedTask)
    startedTask.aggregateId
  }
}
