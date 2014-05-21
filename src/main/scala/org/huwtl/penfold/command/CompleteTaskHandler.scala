package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.Task
import org.huwtl.penfold.domain.store.DomainRepository

case class CompleteTaskHandler(eventStore: DomainRepository) extends CommandHandler[CompleteTask] {
  override def handle(command: CompleteTask) = {
    val completedTask = eventStore.getById[Task](command.id).complete
    eventStore.add(completedTask)
    completedTask.aggregateId
  }
}
