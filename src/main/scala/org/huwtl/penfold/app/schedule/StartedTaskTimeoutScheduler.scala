package org.huwtl.penfold.app.schedule

import org.huwtl.penfold.command.{RequeueTask, CommandDispatcher}
import org.huwtl.penfold.readstore.{TaskProjectionReference, ReadStore}
import org.huwtl.penfold.domain.exceptions.AggregateConflictException
import grizzled.slf4j.Logger
import scala.util.Try
import org.huwtl.penfold.app.StartedTaskTimeoutConfiguration

import org.huwtl.penfold.domain.model.Status.Started

class StartedTaskTimeoutScheduler(readStore: ReadStore, commandDispatcher: CommandDispatcher, config: StartedTaskTimeoutConfiguration) extends Scheduler {
  private lazy val logger = Logger(getClass)

  override val name = "started task timeout"

  override val frequency = config.checkFrequency

  private val reason = "STARTED_TASK_TIMEOUT"

  override def process() {
    readStore.forEachTimedOutTask(Started, config.timeout, requeueTask)
  }

  private def requeueTask(task: TaskProjectionReference) {
    Try(commandDispatcher.dispatch(RequeueTask(task.id, task.version, Some(reason), None, None, None))) recover {
      case e: AggregateConflictException => logger.info(s"conflict on timeout of started task with scheduler $name", e)
      case e: Exception => logger.error(s"error on timeout of started task with scheduler $name", e)
    }
  }
}
