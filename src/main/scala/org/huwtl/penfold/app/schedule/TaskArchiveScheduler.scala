package org.huwtl.penfold.app.schedule

import org.huwtl.penfold.command.{ArchiveTask, CommandDispatcher}
import org.huwtl.penfold.readstore.{TaskProjectionReference, ReadStore}
import org.huwtl.penfold.domain.exceptions.AggregateConflictException
import grizzled.slf4j.Logger
import scala.util.Try
import org.huwtl.penfold.app.TaskArchiverConfiguration

class TaskArchiveScheduler(readStore: ReadStore, commandDispatcher: CommandDispatcher, archiverConfig: TaskArchiverConfiguration) extends Scheduler {
  private lazy val logger = Logger(getClass)

  override val name = "task archiver"

  override val frequency = archiverConfig.checkFrequency

  override def process() {
    //readStore.retrieveTasksToTimeout(absolutePayloadPath(archiverConfig.timeoutPayloadPath)).foreach(archiveTask)
  }

  private def archiveTask(task: TaskProjectionReference) {
    Try(commandDispatcher.dispatch(ArchiveTask(task.id, task.version))) recover {
      case e: AggregateConflictException => logger.info("conflict archiving task", e)
      case e: Exception => logger.error("error archiving task", e)
    }
  }
}
