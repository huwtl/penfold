package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.store.DomainRepository
import org.huwtl.penfold.command.support.AggregateIdFactory
import org.huwtl.penfold.domain.model.{Task, AggregateId, Payload, Binding}
import org.specs2.specification.Scope

class CreateTaskHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val domainRepository = mock[DomainRepository]

  val aggregateIdFactory = mock[AggregateIdFactory]

  trait context extends Scope {
    aggregateIdFactory.create returns expectedAggregateId
  }

  "create task" in new context {
    val handler = new CreateTaskHandler(domainRepository, aggregateIdFactory)

    val aggregateId = handler.handle(new CreateTask(Binding(Nil), Payload.empty))

    there was one(domainRepository).add(any[Task])
    aggregateId must beEqualTo(expectedAggregateId)
  }
}
