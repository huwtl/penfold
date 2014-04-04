package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model.{QueueId, Job, AggregateId}
import org.huwtl.penfold.domain.store.DomainRepository

class StartJobHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val queue = QueueId("q1")

  val domainRepository = mock[DomainRepository]

  val readyJob = mock[Job]
  val startedJob = mock[Job]

  val handler = new StartJobHandler(domainRepository)

  "start ready job" in {
    domainRepository.getById[Job](expectedAggregateId) returns readyJob
    readyJob.start(queue) returns startedJob

    handler.handle(new StartJob(expectedAggregateId, queue))

    there was one(domainRepository).add(startedJob)
  }
}
