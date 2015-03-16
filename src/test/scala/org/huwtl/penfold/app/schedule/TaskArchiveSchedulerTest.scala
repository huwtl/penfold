package org.huwtl.penfold.app.schedule

import org.specs2.mutable.Specification
import org.huwtl.penfold.readstore.{TaskProjectionReference, ReadStore}
import org.specs2.mock.Mockito
import org.huwtl.penfold.command.{ArchiveTask, CommandDispatcher}
import org.huwtl.penfold.app.TaskArchiverConfiguration
import org.huwtl.penfold.support.TestModel

class TaskArchiveSchedulerTest extends Specification with Mockito {

  "periodically archive old tasks" in {
    val readStore = mock[ReadStore]
    val commandDispatcher = mock[CommandDispatcher]
    val archiverConfig = TaskArchiverConfiguration("timeout")
    //readStore.retrieveTasksToTimeout("payload.timeout") returns List(TaskProjectionReference(TestModel.aggregateId, TestModel.version)).toIterator

    new TaskArchiveScheduler(readStore, commandDispatcher, archiverConfig).process()

    there was one(commandDispatcher).dispatch(ArchiveTask(TestModel.aggregateId, TestModel.version))
  }
}
