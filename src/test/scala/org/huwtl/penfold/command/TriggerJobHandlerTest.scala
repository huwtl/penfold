package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model.{Job, AggregateId}
import org.huwtl.penfold.domain.store.DomainRepository

class TriggerJobHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val domainRepository = mock[DomainRepository]

  val createdJob = mock[Job]
  val readyJob = mock[Job]

  val handler = new TriggerJobHandler(domainRepository)

  "trigger waiting job" in {
    domainRepository.getById[Job](expectedAggregateId) returns createdJob
    createdJob.trigger returns readyJob

    handler.handle(new TriggerJob(expectedAggregateId))

    there was one(domainRepository).add(readyJob)
  }
}
