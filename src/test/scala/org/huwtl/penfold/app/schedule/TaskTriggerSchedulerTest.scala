package org.huwtl.penfold.app.schedule

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.readstore.{TaskRecordReference, ReadStore}
import org.huwtl.penfold.command.{TriggerTask, CommandDispatcher}
import org.huwtl.penfold.support.TestModel

class TaskTriggerSchedulerTest extends Specification with Mockito {
  "periodically trigger future tasks" in {
    val readStore = mock[ReadStore]
    val commandDispatcher = mock[CommandDispatcher]
    readStore.retrieveTasksToTrigger returns List(TaskRecordReference(TestModel.aggregateId)).toIterator

    new TaskTriggerScheduler(readStore, commandDispatcher, null).process()

    there was one(commandDispatcher).dispatch(TriggerTask(TestModel.aggregateId))
  }
}
