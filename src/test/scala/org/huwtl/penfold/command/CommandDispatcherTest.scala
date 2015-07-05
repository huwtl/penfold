package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.{AggregateId, AggregateVersion}
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationWithJUnit

class CommandDispatcherTest extends SpecificationWithJUnit with Mockito {

  val triggerTaskCommand = TriggerTask(AggregateId("a1"), AggregateVersion.init)
  val startTaskCommand = StartTask(AggregateId("a1"), AggregateVersion.init, None, None)
  val closeTaskCommand = CloseTask(AggregateId("a1"), AggregateVersion.init, None, None, None, None)

  val triggerTaskHandler = mock[TriggerTaskHandler]
  val startTaskHandler = mock[StartTaskHandler]

  val dispatch = new CommandDispatcher(Map[Class[_ <: Command], CommandHandler[_ <: Command]](//
    classOf[TriggerTask] -> triggerTaskHandler, //
    classOf[StartTask] -> startTaskHandler //
  ))

  "dispatch command to correct handler" in  {
    dispatch.dispatch(triggerTaskCommand)

    there was one(triggerTaskHandler).handle(triggerTaskCommand)
    there were noCallsTo(startTaskHandler)
  }

  "throw exception when no suitable handler" in {
    dispatch.dispatch(closeTaskCommand) must throwA[RuntimeException]
  }
}
