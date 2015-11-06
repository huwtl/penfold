package com.qmetric.penfold.command

import com.qmetric.penfold.domain.model.{AggregateId, AggregateVersion, Task}
import com.qmetric.penfold.domain.store.DomainRepository
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationWithJUnit

class CloseTaskHandlerTest extends SpecificationWithJUnit with Mockito {

  val expectedAggregateId = AggregateId("a1")

  val version = AggregateVersion.init

  val domainRepository = mock[DomainRepository]

  val startedTask = mock[Task]
  val closedTask = mock[Task]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, null).create

  "close task" in {
    domainRepository.getById[Task](expectedAggregateId) returns startedTask
    startedTask.close(version, None, None, None, None) returns closedTask

    commandDispatcher.dispatch(CloseTask(expectedAggregateId, version, None, None, None, None))

    there was one(domainRepository).add(closedTask)
  }
}
