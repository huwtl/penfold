package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model.{Task, AggregateId}
import org.huwtl.penfold.domain.store.DomainRepository

class CompleteTaskHandlerTest extends Specification with Mockito {

  val expectedAggregateId = AggregateId("a1")

  val domainRepository = mock[DomainRepository]

  val startedTask = mock[Task]
  val completedTask = mock[Task]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, null).create

  "complete started task" in {
    domainRepository.getById[Task](expectedAggregateId) returns startedTask
    startedTask.complete() returns completedTask

    commandDispatcher.dispatch(CompleteTask(expectedAggregateId))

    there was one(domainRepository).add(completedTask)
  }
}
