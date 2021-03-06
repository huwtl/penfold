package org.huwtl.penfold.command

import org.huwtl.penfold.domain.store.DomainRepository
import org.huwtl.penfold.domain.model.Task

case class UpdateTaskPayloadHandler(eventStore: DomainRepository) extends CommandHandler[UpdateTaskPayload] {
  override def handle(command: UpdateTaskPayload) = {
    val updatedTask = eventStore.getById[Task](command.id).updatePayload(command.version, command.payloadUpdate, command.updateType, command.scoreUpdate)
    eventStore.add(updatedTask)
    updatedTask.aggregateId
  }
}
