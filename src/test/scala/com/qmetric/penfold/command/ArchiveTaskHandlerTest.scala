package com.qmetric.penfold.command

import org.specs2.mutable.Specification
import com.qmetric.penfold.domain.model.{Task, AggregateId}
import com.qmetric.penfold.domain.store.DomainRepository
import org.specs2.mock.Mockito

class ArchiveTaskHandlerTest extends Specification with Mockito
{
  val expectedAggregateId = AggregateId("a1")

  val domainRepository = mock[DomainRepository]

  val startedTask = mock[Task]

  val archivedTask = mock[Task]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, null).create

  "archive task" in
  {
    domainRepository.getById[Task](expectedAggregateId) returns startedTask
    startedTask.archive returns archivedTask

    commandDispatcher.dispatch(ArchiveTask(expectedAggregateId))

    there was one(domainRepository).add(archivedTask)
  }
}
