package com.qmetric.penfold.command

import com.qmetric.penfold.domain.model.{AggregateId, AggregateVersion, Task}
import com.qmetric.penfold.domain.store.DomainRepository
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationWithJUnit

class TriggerTaskHandlerTest extends SpecificationWithJUnit with Mockito {
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
