package com.qmetric.penfold.app.schedule

import com.qmetric.penfold.command.{CommandDispatcher, TriggerTask}
import com.qmetric.penfold.readstore.{TaskRecordReference, ReadStore}
import scala.concurrent.duration.FiniteDuration
import com.qmetric.penfold.domain.exceptions.AggregateConflictException
import grizzled.slf4j.Logger
import scala.util.Try

class TaskTriggerScheduler(readStore: ReadStore, commandDispatcher: CommandDispatcher, override val frequency: FiniteDuration) extends Scheduler {
  private lazy val logger = Logger(getClass)

  override val name: String = "task trigger"

  override def process() {
    readStore.retrieveTasksToTrigger.foreach(triggerTask)
  }

  private def triggerTask(task: TaskRecordReference) {
    Try(commandDispatcher.dispatch(TriggerTask(task.id))) recover {
      case e: AggregateConflictException => logger.info("conflict triggering task", e)
      case e: Exception => logger.error("error triggering task", e)
    }
  }
}
