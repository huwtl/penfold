package com.qmetric.penfold.command

import com.qmetric.penfold.domain.model.{User, AggregateVersion, AggregateId, Task}
import com.qmetric.penfold.domain.store.DomainRepository
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class ReassignTaskHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val assignee = User("user1")

  val domainRepository = mock[DomainRepository]

  val startedTask = mock[Task]
  val reassignedTask = mock[Task]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, null).create

  "reassign task" in {
    domainRepository.getById[Task](expectedAggregateId) returns startedTask
    startedTask.reassign(AggregateVersion.init, assignee, None, None) returns reassignedTask

    commandDispatcher.dispatch(ReassignTask(expectedAggregateId, AggregateVersion.init, assignee, None, None))

    there was one(domainRepository).add(reassignedTask)
  }
}
