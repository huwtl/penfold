package org.huwtl.penfold.app.schedule

import org.huwtl.penfold.command.CommandDispatcher
import org.huwtl.penfold.readstore.{ReadStore, TaskProjectionReference}
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
