package com.qmetric.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.qmetric.penfold.domain.model.{Task, AggregateId}
import com.qmetric.penfold.domain.store.DomainRepository

class TriggerTaskHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val domainRepository = mock[DomainRepository]

  val createdTask = mock[Task]
  val readyTask = mock[Task]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, null).create

  "trigger waiting task" in {
    domainRepository.getById[Task](expectedAggregateId) returns createdTask
    createdTask.trigger returns readyTask

    commandDispatcher.dispatch(new TriggerTask(expectedAggregateId))

    there was one(domainRepository).add(readyTask)
  }
}
