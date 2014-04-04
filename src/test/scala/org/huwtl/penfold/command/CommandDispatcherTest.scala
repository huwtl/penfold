package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model.{QueueId, AggregateId}

class CommandDispatcherTest extends Specification with Mockito {

  val triggerJobCommand = TriggerJob(AggregateId("a1"))
  val startJobCommand = StartJob(AggregateId("a1"), QueueId("q1"))
  val completeJobCommand = CompleteJob(AggregateId("a1"), QueueId("q1"))

  val triggerJobHandler = mock[TriggerJobHandler]
  val startJobHandler = mock[StartJobHandler]

  val dispatch = new CommandDispatcher(Map[Class[_ <: Command], CommandHandler[_ <: Command]](//
    classOf[TriggerJob] -> triggerJobHandler, //
    classOf[StartJob] -> startJobHandler //
  ))

  "dispatch command to correct handler" in  {
    dispatch.dispatch[TriggerJob](triggerJobCommand)

    there was one(triggerJobHandler).handle(triggerJobCommand)
    there were noCallsTo(startJobHandler)
  }

  "throw exception when no suitable handler" in {
    dispatch.dispatch[CompleteJob](completeJobCommand) must throwA[RuntimeException]
  }
}
