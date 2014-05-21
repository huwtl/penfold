package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model.{Task, QueueId, AggregateId}
import org.huwtl.penfold.domain.store.DomainRepository

class CancelTaskHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val queue = QueueId("q1")

  val domainRepository = mock[DomainRepository]

  val readyTask = mock[Task]
  val cancelledTask = mock[Task]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, null).create

  "cancel task" in {
    domainRepository.getById[Task](expectedAggregateId) returns readyTask
    readyTask.cancel returns cancelledTask

    commandDispatcher.dispatch(CancelTask(expectedAggregateId))

    there was one(domainRepository).add(cancelledTask)
  }
}
