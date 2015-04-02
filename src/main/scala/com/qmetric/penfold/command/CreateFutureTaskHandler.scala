package com.qmetric.penfold.command

import com.qmetric.penfold.domain.model.Task
import com.qmetric.penfold.domain.store.DomainRepository

case class CreateFutureTaskHandler(eventStore: DomainRepository, idFactory: AggregateIdFactory) extends CommandHandler[CreateFutureTask] {
  override def handle(command: CreateFutureTask) = {
    val task = Task.create(idFactory.create, command.queue, command.triggerDate, command.payload, command.score)
    eventStore.add(task)
    task.aggregateId
  }
}
