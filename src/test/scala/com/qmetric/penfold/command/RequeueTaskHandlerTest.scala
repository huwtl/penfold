package com.qmetric.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.qmetric.penfold.domain.model.{Task, AggregateId}
import com.qmetric.penfold.domain.store.DomainRepository

class RequeueTaskHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val domainRepository = mock[DomainRepository]

  val startedTask = mock[Task]
  val requeuedTask = mock[Task]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, null).create

  "requeue task" in {
    domainRepository.getById[Task](expectedAggregateId) returns startedTask
    startedTask.requeue returns requeuedTask

    commandDispatcher.dispatch(RequeueTask(expectedAggregateId))

    there was one(domainRepository).add(requeuedTask)
  }
}
