package com.qmetric.penfold.app.schedule

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.qmetric.penfold.readstore.{TaskRecordReference, ReadStore}
import com.qmetric.penfold.command.{TriggerTask, CommandDispatcher}
import com.qmetric.penfold.support.TestModel

class TaskTriggerSchedulerTest extends Specification with Mockito {
  "periodically trigger future tasks" in {
    val readStore = mock[ReadStore]
    val commandDispatcher = mock[CommandDispatcher]
    readStore.retrieveTasksToTrigger returns List(TaskRecordReference(TestModel.aggregateId, TestModel.version)).toIterator

    new TaskTriggerScheduler(readStore, commandDispatcher, null).process()

    there was one(commandDispatcher).dispatch(TriggerTask(TestModel.aggregateId, TestModel.version))
  }
}
