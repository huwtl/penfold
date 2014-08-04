package com.qmetric.penfold.app.schedule

import com.qmetric.penfold.command.{UnassignTask, CommandDispatcher}
import com.qmetric.penfold.readstore.{TaskRecordReference, ReadStore}
import com.qmetric.penfold.domain.exceptions.AggregateConflictException
import grizzled.slf4j.Logger
import scala.util.Try
import com.qmetric.penfold.app.TaskAssignmentTimeoutConfiguration
import com.qmetric.penfold.domain.model.Status.Ready
import com.qmetric.penfold.domain.model.patch.{Remove, Patch}

class ReadyTaskAssignmentTimeoutScheduler(readStore: ReadStore, commandDispatcher: CommandDispatcher, config: TaskAssignmentTimeoutConfiguration) extends Scheduler {
  private lazy val logger = Logger(getClass)

  private val unassignType = "TIMEOUT"

  private val removeTimeoutPatch = Patch(List(Remove(config.timeoutPayloadPath)))

  override val name = "ready task assignment timeout"

  override val frequency = config.checkFrequency

  override def process() {
    readStore.retrieveTasksToTimeout(absolutePayloadPath(config.timeoutPayloadPath), Some(Ready)).foreach(unassignTask)
  }

  private def unassignTask(task: TaskRecordReference) {
    Try(commandDispatcher.dispatch(UnassignTask(task.id, task.version, Some(unassignType), Some(removeTimeoutPatch)))) recover {
      case e: AggregateConflictException => logger.info(s"conflict unassigning task with scheduler $name", e)
      case e: Exception => logger.error(s"error unassigning task with scheduler $name", e)
    }
  }
}
