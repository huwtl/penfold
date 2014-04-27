package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.Task
import org.huwtl.penfold.domain.store.DomainRepository

case class CreateFutureTaskHandler(eventStore: DomainRepository, idFactory: AggregateIdFactory) extends CommandHandler[CreateFutureTask] {
  override def handle(command: CreateFutureTask) = {
    val task = Task.create(idFactory.create, command.queueBinding, command.triggerDate, command.payload, command.score)
    eventStore.add(task)
    task.aggregateId
  }
}
