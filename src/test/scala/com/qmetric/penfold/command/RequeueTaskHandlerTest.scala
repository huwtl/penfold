package com.qmetric.penfold.command

import com.qmetric.penfold.domain.model.{AggregateId, AggregateVersion, Task}
import com.qmetric.penfold.domain.store.DomainRepository
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationWithJUnit

class RequeueTaskHandlerTest extends SpecificationWithJUnit with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val domainRepository = mock[DomainRepository]

  val startedTask = mock[Task]
  val requeuedTask = mock[Task]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, null).create

  "requeue task" in {
    domainRepository.getById[Task](expectedAggregateId) returns startedTask
    startedTask.requeue(AggregateVersion.init, None, None, None, None) returns requeuedTask

    commandDispatcher.dispatch(RequeueTask(expectedAggregateId, AggregateVersion.init, None, None, None, None))

    there was one(domainRepository).add(requeuedTask)
  }
}
