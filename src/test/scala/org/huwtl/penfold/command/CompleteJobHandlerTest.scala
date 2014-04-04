package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model.{QueueId, Job, AggregateId}
import org.huwtl.penfold.domain.store.DomainRepository

class CompleteJobHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val queue = QueueId("q1")

  val domainRepository = mock[DomainRepository]

  val startedJob = mock[Job]
  val completedJob = mock[Job]

  val handler = new CompleteJobHandler(domainRepository)

  "complete started job" in {
    domainRepository.getById[Job](expectedAggregateId) returns startedJob
    startedJob.complete(queue) returns completedJob

    handler.handle(new CompleteJob(expectedAggregateId, queue))

    there was one(domainRepository).add(completedJob)
  }
}
