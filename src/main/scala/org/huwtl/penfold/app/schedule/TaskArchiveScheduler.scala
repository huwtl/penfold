package org.huwtl.penfold.app.schedule

import org.huwtl.penfold.command.{ArchiveTask, CommandDispatcher}
import org.huwtl.penfold.readstore.{TaskProjectionReference, ReadStore}
import org.huwtl.penfold.domain.exceptions.AggregateConflictException
import grizzled.slf4j.Logger
import scala.util.Try
import org.huwtl.penfold.app.TaskArchiverConfiguration
import org.huwtl.penfold.domain.model.Status.{Cancelled, Closed}

class TaskArchiveScheduler(readStore: ReadStore, commandDispatcher: CommandDispatcher, config: TaskArchiverConfiguration) extends Scheduler {
  private lazy val logger = Logger(getClass)

  override val name = "task archive scheduler"

  override val frequency = config.checkFrequency

  override def process() {
    readStore.forEachTimedOutTask(Closed, config.timeout, archiveTask)
    readStore.forEachTimedOutTask(Cancelled, config.timeout, archiveTask)
  }

  private def archiveTask(task: TaskProjectionReference) {
    Try(commandDispatcher.dispatch(ArchiveTask(task.id, task.version))) recover {
      case e: AggregateConflictException => logger.info("conflict archiving task", e)
      case e: Exception => logger.error("error archiving task", e)
    }
  }
}
