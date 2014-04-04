package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model.{Job, QueueId, AggregateId}
import org.huwtl.penfold.domain.store.DomainRepository

class CancelJobHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val queue = QueueId("q1")

  val domainRepository = mock[DomainRepository]

  val readyJob = mock[Job]
  val cancelledJob = mock[Job]

  val handler = new CancelJobHandler(domainRepository)

  "cancel job" in {
    domainRepository.getById[Job](expectedAggregateId) returns readyJob
    readyJob.cancel(queue) returns cancelledJob

    handler.handle(new CancelJob(expectedAggregateId, queue))

    there was one(domainRepository).add(cancelledJob)
  }
}
