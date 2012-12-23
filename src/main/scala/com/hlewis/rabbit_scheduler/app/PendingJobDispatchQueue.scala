package com.hlewis.rabbit_scheduler.app

import actors.Actor
import org.slf4j.LoggerFactory
import com.hlewis.rabbit_scheduler.usecases.DispatchPendingJobsFromJobStoreToJobExchange

class PendingJobDispatchQueue(dispatchPendingJobsFromStoreToExchange: DispatchPendingJobsFromJobStoreToJobExchange) extends Actor {
  val LOGGER = LoggerFactory.getLogger(getClass)

  def act() {
    loop {
      react {
        case DispatchPending => {
          dispatchPendingJobsFromStoreToExchange.dispatchPending()
        }
        case Quit => exit()
      }
    }
  }

  override def exceptionHandler = {
    case e: Exception => {
      LOGGER.error("Exception occurred while attempting to dispatch pending jobs to exchange: ", e)
    }
  }
}

case object DispatchPending

case object Quit
