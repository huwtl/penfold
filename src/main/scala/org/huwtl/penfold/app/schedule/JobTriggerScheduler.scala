package org.huwtl.penfold.app.schedule

import java.util.concurrent.Executors._
import org.huwtl.penfold.command.{CommandDispatcher, TriggerJob}
import org.huwtl.penfold.readstore.{JobRecordReference, ReadStore}
import scala.concurrent.duration.FiniteDuration
import org.huwtl.penfold.domain.exceptions.AggregateConflictException
import grizzled.slf4j.Logger

class JobTriggerScheduler(readStore: ReadStore, commandDispatcher: CommandDispatcher, frequency: FiniteDuration) {
  private lazy val logger = Logger(getClass)

  def start() = {
    newSingleThreadScheduledExecutor.scheduleAtFixedRate(new Runnable() {
      def run() {
        try {
          logger.debug("job trigger check started")

          readStore.retrieveJobsToTrigger.foreach(triggerJob)

          logger.debug("job trigger check completed")
        }
        catch {
          case e: Exception => logger.error("error during scheduled job trigger check", e)
        }
      }
    }, 0, frequency.length, frequency.unit)
  }

  private def triggerJob(job: JobRecordReference) {
    try {
      commandDispatcher.dispatch[TriggerJob](TriggerJob(job.id))
    }
    catch {
      case e: AggregateConflictException => logger.info("conflict triggering job", e)
      case e: Exception => logger.error("error triggering job", e)
    }
  }
}
