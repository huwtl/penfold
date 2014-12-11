package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model.{AggregateVersion, Task, AggregateId}
import org.huwtl.penfold.domain.store.DomainRepository

class TriggerTaskHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val expectedVersion = AggregateVersion.init

  val domainRepository = mock[DomainRepository]

  val createdTask = mock[Task]
  val readyTask = mock[Task]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, null).create

  "trigger waiting task" in {
    domainRepository.getById[Task](expectedAggregateId) returns createdTask
    createdTask.trigger(expectedVersion) returns readyTask

    commandDispatcher.dispatch(new TriggerTask(expectedAggregateId, expectedVersion))

    there was one(domainRepository).add(readyTask)
  }
}
