package com.qmetric.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.qmetric.penfold.domain.model.{AggregateVersion, Task, AggregateId}
import com.qmetric.penfold.domain.store.DomainRepository
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CloseTaskHandlerTest extends Specification with Mockito {

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
