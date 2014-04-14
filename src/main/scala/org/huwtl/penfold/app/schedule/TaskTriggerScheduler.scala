package org.huwtl.penfold.app.schedule

import java.util.concurrent.Executors._
import org.huwtl.penfold.command.{CommandDispatcher, TriggerTask}
import org.huwtl.penfold.readstore.{TaskRecordReference, ReadStore}
import scala.concurrent.duration.FiniteDuration
import org.huwtl.penfold.domain.exceptions.AggregateConflictException
import grizzled.slf4j.Logger
import scala.util.Try

class TaskTriggerScheduler(readStore: ReadStore, commandDispatcher: CommandDispatcher, frequency: FiniteDuration) {
  private lazy val logger = Logger(getClass)

  def start() = {
    newSingleThreadScheduledExecutor.scheduleAtFixedRate(new Runnable() {
      def run() {
        try {
          logger.debug("task trigger check started")

          readStore.retrieveTasksToTrigger.foreach(triggerTask)

          logger.debug("task trigger check completed")

        } catch {
          case e: Exception => logger.error("error during scheduled task trigger check", e)
        }
      }
    }, 0, frequency.length, frequency.unit)
  }

  private def triggerTask(task: TaskRecordReference) {
    Try(commandDispatcher.dispatch[TriggerTask](TriggerTask(task.id))) recover {
      case e: AggregateConflictException => logger.info("conflict triggering task", e)
      case e: Exception => logger.error("error triggering task", e)
    }
  }
}
