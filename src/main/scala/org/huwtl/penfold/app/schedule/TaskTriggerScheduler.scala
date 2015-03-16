package org.huwtl.penfold.app.schedule

import org.huwtl.penfold.command.{CommandDispatcher, TriggerTask}
import org.huwtl.penfold.readstore.{TaskProjectionReference, ReadStore}
import scala.concurrent.duration.FiniteDuration
import org.huwtl.penfold.domain.exceptions.AggregateConflictException
import grizzled.slf4j.Logger
import scala.util.Try

class TaskTriggerScheduler(readStore: ReadStore, commandDispatcher: CommandDispatcher, override val frequency: FiniteDuration) extends Scheduler {
  private lazy val logger = Logger(getClass)

  override val name: String = "task trigger"

  override def process() {
    readStore.forEachTriggeredTask(triggerTask)
  }

  private def triggerTask(task: TaskProjectionReference) {
    Try(commandDispatcher.dispatch(TriggerTask(task.id, task.version))) recover {
      case e: AggregateConflictException => logger.info("conflict triggering task", e)
      case e: Exception => logger.error("error triggering task", e)
    }
  }
}
