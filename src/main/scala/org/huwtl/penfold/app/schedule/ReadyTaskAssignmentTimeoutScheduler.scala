package org.huwtl.penfold.app.schedule

import org.huwtl.penfold.command.{UnassignTask, CommandDispatcher}
import org.huwtl.penfold.readstore.{TaskProjectionReference, ReadStore}
import org.huwtl.penfold.domain.exceptions.AggregateConflictException
import grizzled.slf4j.Logger
import scala.util.Try
import org.huwtl.penfold.app.TaskAssignmentTimeoutConfiguration
import org.huwtl.penfold.domain.model.Status.Ready
import org.huwtl.penfold.domain.model.patch.{Remove, Patch}

class ReadyTaskAssignmentTimeoutScheduler(readStore: ReadStore, commandDispatcher: CommandDispatcher, config: TaskAssignmentTimeoutConfiguration) extends Scheduler {
  private lazy val logger = Logger(getClass)

  private val unassignType = "TIMEOUT"

  private val removeTimeoutPatch = Patch(List(Remove(config.timeoutPayloadPath)))

  override val name = "ready task assignment timeout"

  override val frequency = config.checkFrequency

  override def process() {
    //readStore.retrieveTasksToTimeout(absolutePayloadPath(config.timeoutPayloadPath), Some(Ready)).foreach(unassignTask)
  }

  private def unassignTask(task: TaskProjectionReference) {
    Try(commandDispatcher.dispatch(UnassignTask(task.id, task.version, Some(unassignType), Some(removeTimeoutPatch)))) recover {
      case e: AggregateConflictException => logger.info(s"conflict unassigning task with scheduler $name", e)
      case e: Exception => logger.error(s"error unassigning task with scheduler $name", e)
    }
  }
}
