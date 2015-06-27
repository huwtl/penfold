package com.qmetric.penfold.command

import com.qmetric.penfold.domain.model.{AggregateId, _}
import com.qmetric.penfold.domain.store.DomainRepository
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationWithJUnit

class CreateTaskHandlerTest extends SpecificationWithJUnit with Mockito {

  val expectedAggregateId = AggregateId("a1")

  val domainRepository = mock[DomainRepository]

  val aggregateIdFactory = mock[AggregateIdFactory]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, aggregateIdFactory).create

  "create task" in {
    aggregateIdFactory.create returns expectedAggregateId

    val aggregateId = commandDispatcher.dispatch(new CreateTask(QueueId("q1"), Payload.empty, None))

    there was one(domainRepository).add(any[Task])
    aggregateId must beEqualTo(expectedAggregateId)
  }
}
