package org.huwtl.penfold.app.schedule

import java.util.concurrent.TimeUnit._

import org.huwtl.penfold.app.TaskArchiverConfiguration
import org.huwtl.penfold.command.CommandDispatcher
import org.huwtl.penfold.domain.model.Status.Closed
import org.huwtl.penfold.readstore.{ReadStore, TaskProjectionReference}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.duration.FiniteDuration

class TaskArchiveSchedulerTest extends Specification with Mockito {

  "archive closed tasks on timeout" in {
    val readStore = mock[ReadStore]
    val commandDispatcher = mock[CommandDispatcher]
    val config = TaskArchiverConfiguration(FiniteDuration(1L, MINUTES))

    new TaskArchiveScheduler(readStore, commandDispatcher, config).process()

    there was one(readStore).forEachTimedOutTask(===(Closed), ===(FiniteDuration(1L, MINUTES)), any[TaskProjectionReference => Unit])
  }
}
