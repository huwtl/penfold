package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.model.{AggregateVersion, Task, AggregateId}
import org.huwtl.penfold.domain.store.DomainRepository
import org.specs2.mock.Mockito

class UnassignTaskHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val domainRepository = mock[DomainRepository]

  val readyTask = mock[Task]
  val unassignedTask = mock[Task]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, null).create

  "unassign task" in {
    domainRepository.getById[Task](expectedAggregateId) returns readyTask
    readyTask.unassign(AggregateVersion.init, None, None) returns unassignedTask

    commandDispatcher.dispatch(UnassignTask(expectedAggregateId, AggregateVersion.init, None, None))

    there was one(domainRepository).add(unassignedTask)
  }
}
