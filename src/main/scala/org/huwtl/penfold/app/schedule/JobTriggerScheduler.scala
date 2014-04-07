package org.huwtl.penfold.app.schedule

import java.util.concurrent.Executors._
import org.huwtl.penfold.command.{CommandDispatcher, TriggerJob}
import org.huwtl.penfold.readstore.{JobRecordReference, ReadStore}
import org.slf4j.LoggerFactory
import scala.concurrent.duration.FiniteDuration
import org.huwtl.penfold.domain.exceptions.AggregateConflictException

class JobTriggerScheduler(readStore: ReadStore, commandDispatcher: CommandDispatcher, frequency: FiniteDuration) {
  private val logger = LoggerFactory.getLogger(getClass)

  def start() = {
    newSingleThreadScheduledExecutor.scheduleAtFixedRate(new Runnable() {
      def run() {
        try {
          readStore.retrieveJobsToTrigger.foreach(triggerJob)
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
