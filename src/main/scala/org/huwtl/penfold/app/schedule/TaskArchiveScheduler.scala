package org.huwtl.penfold.app.schedule

import java.util.concurrent.Executors._
import org.huwtl.penfold.command.{ArchiveTask, CommandDispatcher}
import org.huwtl.penfold.readstore.{TaskRecordReference, ReadStore}
import org.huwtl.penfold.domain.exceptions.AggregateConflictException
import grizzled.slf4j.Logger
import scala.util.Try
import org.huwtl.penfold.app.TaskArchiverConfiguration

class TaskArchiveScheduler(readStore: ReadStore, commandDispatcher: CommandDispatcher, archiverConfig: TaskArchiverConfiguration) {
  private lazy val logger = Logger(getClass)

  def start() = {
    newSingleThreadScheduledExecutor.scheduleAtFixedRate(new Runnable() {
      def run() {
        try {
          logger.debug("task archiver started")

          readStore.retrieveTasksToArchive(archiverConfig.timeoutAttributePath).foreach(archiveTask)

          logger.debug("task archiver completed")

        } catch {
          case e: Exception => logger.error("error during scheduled archiver", e)
        }
      }
    }, 0, archiverConfig.checkFrequency.length, archiverConfig.checkFrequency.unit)
  }

  private def archiveTask(task: TaskRecordReference) {
    Try(commandDispatcher.dispatch(ArchiveTask(task.id))) recover {
      case e: AggregateConflictException => logger.info("conflict archiving task", e)
      case e: Exception => logger.error("error archiving task", e)
    }
  }
}
