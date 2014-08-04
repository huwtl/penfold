package com.qmetric.penfold.app.schedule

import com.qmetric.penfold.command.{ArchiveTask, CommandDispatcher}
import com.qmetric.penfold.readstore.{TaskRecordReference, ReadStore}
import com.qmetric.penfold.domain.exceptions.AggregateConflictException
import grizzled.slf4j.Logger
import scala.util.Try
import com.qmetric.penfold.app.TaskArchiverConfiguration

class TaskArchiveScheduler(readStore: ReadStore, commandDispatcher: CommandDispatcher, archiverConfig: TaskArchiverConfiguration) extends Scheduler {
  private lazy val logger = Logger(getClass)

  override val name = "task archiver"

  override val frequency = archiverConfig.checkFrequency

  override def process() {
    readStore.retrieveTasksToTimeout(absolutePayloadPath(archiverConfig.timeoutPayloadPath)).foreach(archiveTask)
  }

  private def archiveTask(task: TaskRecordReference) {
    Try(commandDispatcher.dispatch(ArchiveTask(task.id, task.version))) recover {
      case e: AggregateConflictException => logger.info("conflict archiving task", e)
      case e: Exception => logger.error("error archiving task", e)
    }
  }
}
