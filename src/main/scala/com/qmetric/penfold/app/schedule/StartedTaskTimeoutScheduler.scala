package com.qmetric.penfold.app.schedule

import com.qmetric.penfold.command.{RequeueTask, CommandDispatcher}
import com.qmetric.penfold.readstore.{TaskProjectionReference, ReadStore}
import com.qmetric.penfold.domain.exceptions.AggregateConflictException
import grizzled.slf4j.Logger
import scala.util.Try
import com.qmetric.penfold.app.StartedTaskTimeoutConfiguration

import com.qmetric.penfold.domain.model.Status.Started

class StartedTaskTimeoutScheduler(readStore: ReadStore, commandDispatcher: CommandDispatcher, config: StartedTaskTimeoutConfiguration) extends Scheduler {
  private lazy val logger = Logger(getClass)

  override val name = "started task timeout"

  override val frequency = config.checkFrequency

  private val reason = "STARTED_TASK_TIMEOUT"

  override def process() {
    readStore.forEachTimedOutTask(Started, config.timeout, requeueTask)
  }

  private def requeueTask(task: TaskProjectionReference) {
    logger.info(s"auto requeuing task ${task.id} due to task taking too long to complete")

    Try(commandDispatcher.dispatch(RequeueTask(task.id, task.version, Some(reason), None, None, None))) recover {
      case e: AggregateConflictException => logger.info(s"conflict on timeout of started task with scheduler $name", e)
      case e: Exception => logger.error(s"error on timeout of started task with scheduler $name", e)
    }
  }
}
