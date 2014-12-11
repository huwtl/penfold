package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model.{Task, AggregateVersion, AggregateId}
import org.huwtl.penfold.domain.store.DomainRepository
import org.huwtl.penfold.domain.model.patch.Patch

class UpdateTaskPayloadHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val originalTask = mock[Task]
  val updatedTask = mock[Task]
  val domainRepository = mock[DomainRepository]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, null).create

  "update task payload" in {
    val command = new UpdateTaskPayload(expectedAggregateId, AggregateVersion.init, Some("update_type_1"), Patch(Nil), Some(100))
    domainRepository.getById[Task](expectedAggregateId) returns originalTask
    originalTask.updatePayload(command.version, command.payloadUpdate, command.updateType, command.scoreUpdate) returns updatedTask

    commandDispatcher.dispatch(UpdateTaskPayload(expectedAggregateId, AggregateVersion.init, Some("update_type_1"), Patch(Nil), Some(100)))

    there was one(domainRepository).add(updatedTask)
  }
}
