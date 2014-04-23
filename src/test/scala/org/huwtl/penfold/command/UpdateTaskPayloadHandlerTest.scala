package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model.{Task, Payload, AggregateVersion, AggregateId}
import org.huwtl.penfold.domain.store.DomainRepository
import org.specs2.specification.Scope

class UpdateTaskPayloadHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val originalTask = mock[Task]
  val updatedTask = mock[Task]
  val domainRepository = mock[DomainRepository]

  val handler = new UpdateTaskPayloadHandler(domainRepository)

  "update task payload" in {
    val command = new UpdateTaskPayload(expectedAggregateId, AggregateVersion.init, Some("update_type_1"), Payload.empty, Some(100))
    domainRepository.getById[Task](expectedAggregateId) returns originalTask
    originalTask.updatePayload(command.version, command.payload, command.updateType, command.score) returns updatedTask

    handler.handle(new UpdateTaskPayload(expectedAggregateId, AggregateVersion.init, Some("update_type_1"), Payload.empty, Some(100)))

    there was one(domainRepository).add(updatedTask)
  }
}
