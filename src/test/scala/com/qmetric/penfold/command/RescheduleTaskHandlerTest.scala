package com.qmetric.penfold.command

import com.qmetric.penfold.domain.model.{AggregateId, AggregateVersion, Task}
import com.qmetric.penfold.domain.store.DomainRepository
import org.joda.time.DateTime
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationWithJUnit

class RescheduleTaskHandlerTest extends SpecificationWithJUnit with Mockito {
  val expectedAggregateId = AggregateId("a1")
  val triggerDate = DateTime.now

  val domainRepository = mock[DomainRepository]

  val startedTask = mock[Task]
  val rescheduledTask = mock[Task]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, null).create

  "reschedule task" in {
    domainRepository.getById[Task](expectedAggregateId) returns startedTask
    startedTask.reschedule(AggregateVersion.init, triggerDate, None, None, None, None) returns rescheduledTask

    commandDispatcher.dispatch(RescheduleTask(expectedAggregateId, AggregateVersion.init, triggerDate, None, None, None, None))

    there was one(domainRepository).add(rescheduledTask)
  }
}
