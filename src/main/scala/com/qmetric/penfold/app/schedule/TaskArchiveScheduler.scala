package com.qmetric.penfold.app.schedule

import com.qmetric.penfold.command.{ArchiveTask, CommandDispatcher}
import com.qmetric.penfold.readstore.{TaskProjectionReference, ReadStore}
import com.qmetric.penfold.domain.exceptions.AggregateConflictException
import grizzled.slf4j.Logger
import scala.util.Try
import com.qmetric.penfold.app.TaskArchiverConfiguration
import com.qmetric.penfold.domain.model.Status.{Cancelled, Closed}

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
