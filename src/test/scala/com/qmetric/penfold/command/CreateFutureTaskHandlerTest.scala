package com.qmetric.penfold.command

import org.specs2.mutable.Specification
import com.qmetric.penfold.domain.model._
import com.qmetric.penfold.domain.store.DomainRepository
import org.specs2.mock.Mockito
import org.joda.time.DateTime
import com.qmetric.penfold.domain.model.AggregateId
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CreateFutureTaskHandlerTest extends Specification with Mockito {

  val expectedAggregateId = AggregateId("a1")

  val domainRepository = mock[DomainRepository]

  val aggregateIdFactory = mock[AggregateIdFactory]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, aggregateIdFactory).create

  "create future task" in {
    aggregateIdFactory.create returns expectedAggregateId

    val aggregateId = commandDispatcher.dispatch(CreateFutureTask(QueueId("q1"), DateTime.now, Payload.empty, None))

    there was one(domainRepository).add(any[Task])
    aggregateId must beEqualTo(expectedAggregateId)
  }
}
