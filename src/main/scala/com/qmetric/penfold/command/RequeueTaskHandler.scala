package com.qmetric.penfold.command

import com.qmetric.penfold.domain.store.DomainRepository
import com.qmetric.penfold.domain.model.Task

case class RequeueTaskHandler(eventStore: DomainRepository) extends CommandHandler[RequeueTask] {
  override def handle(command: RequeueTask) = {
    val requeuedTask = eventStore.getById[Task](command.id).requeue(command.version, command.reason, command.assignee, command.payloadUpdate, command.scoreUpdate)
    eventStore.add(requeuedTask)
    requeuedTask.aggregateId
  }
}
