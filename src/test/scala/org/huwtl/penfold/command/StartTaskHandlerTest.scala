package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model.{QueueId, Task, AggregateId}
import org.huwtl.penfold.domain.store.DomainRepository

class StartTaskHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val queue = QueueId("q1")

  val domainRepository = mock[DomainRepository]

  val readyTask = mock[Task]
  val startedTask = mock[Task]

  val handler = new StartTaskHandler(domainRepository)

  "start ready task" in {
    domainRepository.getById[Task](expectedAggregateId) returns readyTask
    readyTask.start(queue) returns startedTask

    handler.handle(new StartTask(expectedAggregateId, queue))

    there was one(domainRepository).add(startedTask)
  }
}
