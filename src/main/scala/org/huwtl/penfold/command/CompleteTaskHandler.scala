package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.Task
import org.huwtl.penfold.domain.store.DomainRepository

case class CompleteTaskHandler(eventStore: DomainRepository) extends CommandHandler[CompleteTask] {
  override def handle(command: CompleteTask) = {
    val task = eventStore.getById[Task](command.id)
    val completedTask = task.complete(command.queueId)
    eventStore.add(completedTask)
    completedTask.aggregateId
  }
}
