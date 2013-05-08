package com.hlewis.eventfire.app

//import org.slf4j.LoggerFactory

import com.hlewis.eventfire.usecases.RefreshFeedWithPendingJobs
import akka.actor.Actor

class PendingJobDispatchQueue(exposePendingJobsFromStore: RefreshFeedWithPendingJobs) extends Actor {

  //val LOGGER = LoggerFactory.getLogger(getClass)

  override def receive = {
    case DispatchPending => {
      exposePendingJobsFromStore.refresh()
    }
  }

  // todo: not sure how to do this with akka actors yet
  //  {
  //    case e: Exception => LOGGER.error("Exception occurred while attempting to dispatch pending jobs to exchange: ", e)
  //  }
}

case object DispatchPending