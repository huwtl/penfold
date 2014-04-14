package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model.{QueueId, Task, AggregateId}
import org.huwtl.penfold.domain.store.DomainRepository

class CompleteTaskHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val queue = QueueId("q1")

  val domainRepository = mock[DomainRepository]

  val startedTask = mock[Task]
  val completedTask = mock[Task]

  val handler = new CompleteTaskHandler(domainRepository)

  "complete started task" in {
    domainRepository.getById[Task](expectedAggregateId) returns startedTask
    startedTask.complete(queue) returns completedTask

    handler.handle(new CompleteTask(expectedAggregateId, queue))

    there was one(domainRepository).add(completedTask)
  }
}
