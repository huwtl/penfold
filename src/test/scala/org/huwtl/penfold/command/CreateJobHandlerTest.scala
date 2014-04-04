package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.store.DomainRepository
import org.huwtl.penfold.command.support.AggregateIdFactory
import org.huwtl.penfold.domain.model.{Job, AggregateId, Payload, Binding}
import org.specs2.specification.Scope

class CreateJobHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val domainRepository = mock[DomainRepository]

  val aggregateIdFactory = mock[AggregateIdFactory]

  trait context extends Scope {
    aggregateIdFactory.create returns expectedAggregateId
  }

  "create job" in new context {
    val handler = new CreateJobHandler(domainRepository, aggregateIdFactory)

    val aggregateId = handler.handle(new CreateJob(Binding(Nil), Payload(Map())))

    there was one(domainRepository).add(any[Job])
    aggregateId must beEqualTo(expectedAggregateId)
  }
}
