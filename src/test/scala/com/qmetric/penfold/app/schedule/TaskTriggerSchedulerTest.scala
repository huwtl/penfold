package com.qmetric.penfold.app.schedule

import java.util.concurrent.TimeUnit

import com.qmetric.penfold.command.CommandDispatcher
import com.qmetric.penfold.readstore.{ReadStore, TaskProjectionReference}
import org.specs2.mock.Mockito
import org.mockito.Mockito._
import org.specs2.mutable.SpecificationWithJUnit

import scala.concurrent.duration.FiniteDuration

class TaskTriggerSchedulerTest extends SpecificationWithJUnit with Mockito {
  "trigger future tasks" in {
    val readStore = mock[ReadStore]
    val commandDispatcher = mock[CommandDispatcher]

    new TaskTriggerScheduler(readStore, commandDispatcher, null).process()

    there was one(readStore).forEachTriggeredTask(any[TaskProjectionReference => Unit])
  }

  "periodically trigger future tasks even in the case of error" in {
    val brokenReadStore = createBrokenReadStore()
    val commandDispatcher = mock[CommandDispatcher]

    val scheduler: TaskTriggerScheduler = new TaskTriggerScheduler(
      brokenReadStore, commandDispatcher, new FiniteDuration(10, TimeUnit.MILLISECONDS))

    scheduler.start()
    waitLongEnoughSoThatGivenDurationPassesAtLeastTimes(scheduler.frequency, 3)

    there were atLeastThree(brokenReadStore).forEachTriggeredTask(any[TaskProjectionReference => Unit])
  }

  private def waitLongEnoughSoThatGivenDurationPassesAtLeastTimes(delay: FiniteDuration, howManyTimes: Int): Unit = {
    def warmUpTimeInMilliseconds = 500
    Thread.sleep(warmUpTimeInMilliseconds + (delay.toMillis * howManyTimes))
  }

  private def createBrokenReadStore(): ReadStore = {
    val readStore = mock[ReadStore]
    when(readStore.forEachTriggeredTask(any[TaskProjectionReference => Unit])).thenThrow(new Error())

    readStore
  }
}
