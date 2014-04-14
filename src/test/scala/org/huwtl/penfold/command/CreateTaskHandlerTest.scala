package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.store.DomainRepository
import org.huwtl.penfold.command.support.AggregateIdFactory
import org.huwtl.penfold.domain.model._
import org.specs2.specification.Scope
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.QueueBinding

class CreateTaskHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val domainRepository = mock[DomainRepository]

  val aggregateIdFactory = mock[AggregateIdFactory]

  trait context extends Scope {
    aggregateIdFactory.create returns expectedAggregateId
  }

  "create task" in new context {
    val handler = new CreateTaskHandler(domainRepository, aggregateIdFactory)

    val aggregateId = handler.handle(new CreateTask(QueueBinding(QueueId("q1")), Payload.empty))

    there was one(domainRepository).add(any[Task])
    aggregateId must beEqualTo(expectedAggregateId)
  }
}
