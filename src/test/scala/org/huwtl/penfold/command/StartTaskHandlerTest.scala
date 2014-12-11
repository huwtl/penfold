package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model.{User, Task, AggregateId}
import org.huwtl.penfold.domain.store.DomainRepository
import org.huwtl.penfold.support.TestModel

class StartTaskHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val assignee = User("username")

  val domainRepository = mock[DomainRepository]

  val readyTask = mock[Task]
  val startedTask = mock[Task]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, null).create

  "start ready task" in {
    domainRepository.getById[Task](expectedAggregateId) returns readyTask
    readyTask.start(TestModel.version, Some(assignee), Some(TestModel.payloadUpdate)) returns startedTask

    commandDispatcher.dispatch(StartTask(expectedAggregateId, TestModel.version, Some(assignee), Some(TestModel.payloadUpdate)))

    there was one(domainRepository).add(startedTask)
  }
}
