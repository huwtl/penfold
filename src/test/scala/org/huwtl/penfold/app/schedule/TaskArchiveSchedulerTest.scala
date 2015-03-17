package org.huwtl.penfold.app.schedule

import org.specs2.mutable.Specification
import org.huwtl.penfold.readstore.{TaskProjectionReference, ReadStore}
import org.specs2.mock.Mockito
import org.huwtl.penfold.command.{ArchiveTask, CommandDispatcher}
import org.huwtl.penfold.app.{TaskRequeueTimeoutConfiguration, TaskArchiverConfiguration}
import org.huwtl.penfold.support.TestModel
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit._
import org.huwtl.penfold.app.TaskRequeueTimeoutConfiguration
import org.huwtl.penfold.app.TaskArchiverConfiguration
import org.huwtl.penfold.readstore.TaskProjectionReference
import org.huwtl.penfold.command.ArchiveTask
import org.huwtl.penfold.domain.model.Status.{Closed, Started}

class TaskArchiveSchedulerTest extends Specification with Mockito {

  "archive closed tasks on timeout" in {
    val readStore = mock[ReadStore]
    val commandDispatcher = mock[CommandDispatcher]
    val config = TaskArchiverConfiguration(FiniteDuration(1L, MINUTES))

    new TaskArchiveScheduler(readStore, commandDispatcher, config).process()

    there was one(readStore).forEachTimedOutTask(===(Closed), ===(FiniteDuration(1L, MINUTES)), any[TaskProjectionReference => Unit])
  }
}
