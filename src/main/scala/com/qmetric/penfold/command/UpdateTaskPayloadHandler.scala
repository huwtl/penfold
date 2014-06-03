package com.qmetric.penfold.command

import com.qmetric.penfold.domain.store.DomainRepository
import com.qmetric.penfold.domain.model.Task

case class UpdateTaskPayloadHandler(eventStore: DomainRepository) extends CommandHandler[UpdateTaskPayload] {
  override def handle(command: UpdateTaskPayload) = {
    val updatedTask = eventStore.getById[Task](command.id).updatePayload(command.version, command.payloadUpdate, command.updateType, command.score)
    eventStore.add(updatedTask)
    updatedTask.aggregateId
  }
}
