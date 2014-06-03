package com.qmetric.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.qmetric.penfold.domain.model.{Task, AggregateVersion, AggregateId}
import com.qmetric.penfold.domain.store.DomainRepository
import com.qmetric.penfold.domain.model.patch.Patch

class UpdateTaskPayloadHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val originalTask = mock[Task]
  val updatedTask = mock[Task]
  val domainRepository = mock[DomainRepository]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, null).create

  "update task payload" in {
    val command = new UpdateTaskPayload(expectedAggregateId, AggregateVersion.init, Some("update_type_1"), Patch(Nil), Some(100))
    domainRepository.getById[Task](expectedAggregateId) returns originalTask
    originalTask.updatePayload(command.version, command.payloadUpdate, command.updateType, command.score) returns updatedTask

    commandDispatcher.dispatch(UpdateTaskPayload(expectedAggregateId, AggregateVersion.init, Some("update_type_1"), Patch(Nil), Some(100)))

    there was one(domainRepository).add(updatedTask)
  }
}
