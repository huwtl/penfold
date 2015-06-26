package com.qmetric.penfold.app.schedule

import java.util.concurrent.TimeUnit._

import com.qmetric.penfold.app.TaskArchiverConfiguration
import com.qmetric.penfold.command.CommandDispatcher
import com.qmetric.penfold.domain.model.Status.Closed
import com.qmetric.penfold.readstore.{ReadStore, TaskProjectionReference}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.duration.FiniteDuration
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TaskArchiveSchedulerTest extends Specification with Mockito {

  "archive closed tasks on timeout" in {
    val readStore = mock[ReadStore]
    val commandDispatcher = mock[CommandDispatcher]
    val config = TaskArchiverConfiguration(FiniteDuration(1L, MINUTES))

    new TaskArchiveScheduler(readStore, commandDispatcher, config).process()

    there was one(readStore).forEachTimedOutTask(===(Closed), ===(FiniteDuration(1L, MINUTES)), any[TaskProjectionReference => Unit])
  }
}
