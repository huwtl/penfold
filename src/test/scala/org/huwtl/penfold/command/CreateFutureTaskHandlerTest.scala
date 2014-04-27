package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.domain.store.DomainRepository
import org.specs2.specification.Scope
import org.specs2.mock.Mockito
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.QueueBinding

class CreateFutureTaskHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val domainRepository = mock[DomainRepository]

  val aggregateIdFactory = mock[AggregateIdFactory]

  trait context extends Scope {
    aggregateIdFactory.create returns expectedAggregateId
  }

  "create future task" in new context {
    val handler = new CreateFutureTaskHandler(domainRepository, aggregateIdFactory)

    val aggregateId = handler.handle(new CreateFutureTask(QueueBinding(QueueId("q1")), DateTime.now, Payload.empty, None))

    there was one(domainRepository).add(any[Task])
    aggregateId must beEqualTo(expectedAggregateId)
  }
}
