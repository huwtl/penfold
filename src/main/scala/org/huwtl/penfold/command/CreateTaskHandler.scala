package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.Task
import org.huwtl.penfold.domain.store.DomainRepository

case class CreateTaskHandler(eventStore: DomainRepository, idFactory: AggregateIdFactory) extends CommandHandler[CreateTask] {
  override def handle(command: CreateTask) = {
    val task = Task.create(idFactory.create, command.queueBinding, command.payload, command.score)
    eventStore.add(task)
    task.aggregateId
  }
}
