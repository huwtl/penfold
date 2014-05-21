package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model.{Assignee, Task, AggregateId}
import org.huwtl.penfold.domain.store.DomainRepository

class StartTaskHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val assignee = Assignee("username")

  val domainRepository = mock[DomainRepository]

  val readyTask = mock[Task]
  val startedTask = mock[Task]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, null).create

  "start ready task" in {
    domainRepository.getById[Task](expectedAggregateId) returns readyTask
    readyTask.start(Some(assignee)) returns startedTask

    commandDispatcher.dispatch(StartTask(expectedAggregateId, Some(assignee)))

    there was one(domainRepository).add(startedTask)
  }
}
