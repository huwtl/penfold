package com.qmetric.penfold.command

import com.qmetric.penfold.domain.model.{AggregateId, AggregateVersion, Task}
import com.qmetric.penfold.domain.store.DomainRepository
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class CancelTaskHandlerTest extends Specification with Mockito {

  val expectedAggregateId = AggregateId("a1")

  val version = AggregateVersion.init

  val domainRepository = mock[DomainRepository]

  val startedTask = mock[Task]
  val cancelledTask = mock[Task]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, null).create

  "cancel task" in {
    domainRepository.getById[Task](expectedAggregateId) returns startedTask
    startedTask.cancel(version, None, None, None) returns cancelledTask

    commandDispatcher.dispatch(CancelTask(expectedAggregateId, version, None, None, None))

    there was one(domainRepository).add(cancelledTask)
  }
}
