package org.huwtl.penfold.app.schedule

import org.specs2.mutable.Specification
import org.huwtl.penfold.readstore.{TaskProjectionReference, ReadStore}
import org.huwtl.penfold.command.CommandDispatcher
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit._
import org.huwtl.penfold.app.StartedTaskTimeoutConfiguration
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model.Status.Started

class StartedTaskTimeoutSchedulerTest extends Specification with Mockito {

  "requeue started tasks on timeout" in {
    val readStore = mock[ReadStore]
    val commandDispatcher = mock[CommandDispatcher]
    val config = StartedTaskTimeoutConfiguration(FiniteDuration(1L, MINUTES))

    new StartedTaskTimeoutScheduler(readStore, commandDispatcher, config).process()

    there was one(readStore).forEachTimedOutTask(===(Started), ===(FiniteDuration(1L, MINUTES)), any[TaskProjectionReference => Unit])
  }
}
