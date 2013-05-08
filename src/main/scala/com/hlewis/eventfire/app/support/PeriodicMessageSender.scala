package com.hlewis.eventfire.app.support

import akka.actor.{ReceiveTimeout, Actor}
import scala.concurrent.duration._
import com.hlewis.eventfire.usecases.RefreshFeedWithPendingJobs

class PeriodicMessageSender(duration: Duration, refreshFeedWithPendingJobs: RefreshFeedWithPendingJobs) extends Actor {

  context.setReceiveTimeout(duration)

  def receive = {
    case ReceiveTimeout => refreshFeedWithPendingJobs.refresh()
  }
}
