package org.huwtl.penfold.command

import org.huwtl.penfold.domain.store.DomainRepository
import org.huwtl.penfold.domain.model.Task

case class RequeueTaskHandler(eventStore: DomainRepository) extends CommandHandler[RequeueTask] {
  override def handle(command: RequeueTask) = {
    val requeuedTask = eventStore.getById[Task](command.id).requeue(command.version, command.requeueType, command.assignee, command.payloadUpdate, command.scoreUpdate)
    eventStore.add(requeuedTask)
    requeuedTask.aggregateId
  }
}
