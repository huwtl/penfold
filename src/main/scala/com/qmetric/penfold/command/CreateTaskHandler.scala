package com.qmetric.penfold.command

import com.qmetric.penfold.domain.model.Task
import com.qmetric.penfold.domain.store.DomainRepository

case class CreateTaskHandler(eventStore: DomainRepository, idFactory: AggregateIdFactory) extends CommandHandler[CreateTask] {
  override def handle(command: CreateTask) = {
    val task = Task.create(idFactory.create, command.queue, command.payload, command.score)
    eventStore.add(task)
    task.aggregateId
  }
}
