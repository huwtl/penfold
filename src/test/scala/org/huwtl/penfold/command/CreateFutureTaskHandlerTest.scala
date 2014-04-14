package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.model.{Task, Payload, Binding, AggregateId}
import org.huwtl.penfold.domain.store.DomainRepository
import org.huwtl.penfold.command.support.AggregateIdFactory
import org.specs2.specification.Scope
import org.specs2.mock.Mockito
import org.joda.time.DateTime

class CreateFutureTaskHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val domainRepository = mock[DomainRepository]

  val aggregateIdFactory = mock[AggregateIdFactory]

  trait context extends Scope {
    aggregateIdFactory.create returns expectedAggregateId
  }

  "create future task" in new context {
    val handler = new CreateFutureTaskHandler(domainRepository, aggregateIdFactory)

    val aggregateId = handler.handle(new CreateFutureTask(Binding(Nil), DateTime.now, Payload.empty))

    there was one(domainRepository).add(any[Task])
    aggregateId must beEqualTo(expectedAggregateId)
  }
}
