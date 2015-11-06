package com.qmetric.penfold.app.schedule

import com.qmetric.penfold.command.CommandDispatcher
import com.qmetric.penfold.readstore.{ReadStore, TaskProjectionReference}
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationWithJUnit

class TaskTriggerSchedulerTest extends SpecificationWithJUnit with Mockito {
  "periodically trigger future tasks" in {
    val readStore = mock[ReadStore]
    val commandDispatcher = mock[CommandDispatcher]

    new TaskTriggerScheduler(readStore, commandDispatcher, null).process()

    there was one(readStore).forEachTriggeredTask(any[TaskProjectionReference => Unit])
  }
}
