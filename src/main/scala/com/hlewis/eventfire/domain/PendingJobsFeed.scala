package com.hlewis.eventfire.domain

import akka.actor.Actor

class PendingJobsFeed extends Actor {
  //val LOGGER = LoggerFactory.getLogger(getClass)

  override def receive = {
    case Refresh => {
      println("refresh...")
    }
  }

  // todo: not sure how to do this with akka actors yet
  //  {
  //    case e: Exception => LOGGER.error("Exception occurred while attempting to dispatch pending jobs to exchange: ", e)
  //  }
}

case object Refresh