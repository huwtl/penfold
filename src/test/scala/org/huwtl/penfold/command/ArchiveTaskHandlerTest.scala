package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.model.{AggregateVersion, Task, AggregateId}
import org.huwtl.penfold.domain.store.DomainRepository
import org.specs2.mock.Mockito

class ArchiveTaskHandlerTest extends Specification with Mockito
{
  val expectedAggregateId = AggregateId("a1")

  val expectedVersion = AggregateVersion.init

  val domainRepository = mock[DomainRepository]

  val startedTask = mock[Task]

  val archivedTask = mock[Task]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, null).create

  "archive task" in
  {
    domainRepository.getById[Task](expectedAggregateId) returns startedTask
    startedTask.archive(expectedVersion) returns archivedTask

    commandDispatcher.dispatch(ArchiveTask(expectedAggregateId, expectedVersion))

    there was one(domainRepository).add(archivedTask)
  }
}
