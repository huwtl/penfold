package com.qmetric.penfold.app.schedule

import org.specs2.mutable.Specification
import com.qmetric.penfold.readstore.{TaskRecordReference, ReadStore}
import org.specs2.mock.Mockito
import com.qmetric.penfold.command.{ArchiveTask, CommandDispatcher}
import com.qmetric.penfold.app.TaskArchiverConfiguration
import com.qmetric.penfold.support.TestModel

class TaskArchiveSchedulerTest extends Specification with Mockito {

  "periodically archive old tasks" in {
    val readStore = mock[ReadStore]
    val commandDispatcher = mock[CommandDispatcher]
    val archiverConfig = TaskArchiverConfiguration("timeout")
    readStore.retrieveTasksToTimeout("payload.timeout") returns List(TaskRecordReference(TestModel.aggregateId, TestModel.version)).toIterator

    new TaskArchiveScheduler(readStore, commandDispatcher, archiverConfig).process()

    there was one(commandDispatcher).dispatch(ArchiveTask(TestModel.aggregateId, TestModel.version))
  }
}
