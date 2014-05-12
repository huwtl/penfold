package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model.{Task, AggregateId}
import org.huwtl.penfold.domain.store.DomainRepository

class RequeueTaskHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val domainRepository = mock[DomainRepository]

  val startedTask = mock[Task]
  val requeuedTask = mock[Task]

  val handler = new RequeueTaskHandler(domainRepository)

  "requeue task" in {
    domainRepository.getById[Task](expectedAggregateId) returns startedTask
    startedTask.requeue() returns requeuedTask

    handler.handle(new RequeueTask(expectedAggregateId))

    there was one(domainRepository).add(requeuedTask)
  }
}
