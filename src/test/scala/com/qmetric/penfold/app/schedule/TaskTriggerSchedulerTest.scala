package com.qmetric.penfold.app.schedule

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.qmetric.penfold.readstore.{TaskProjectionReference, ReadStore}
import com.qmetric.penfold.command.CommandDispatcher

class TaskTriggerSchedulerTest extends Specification with Mockito {
  "periodically trigger future tasks" in {
    val readStore = mock[ReadStore]
    val commandDispatcher = mock[CommandDispatcher]

    new TaskTriggerScheduler(readStore, commandDispatcher, null).process()

    there was one(readStore).forEachTriggeredTask(any[TaskProjectionReference => Unit])
  }
}
