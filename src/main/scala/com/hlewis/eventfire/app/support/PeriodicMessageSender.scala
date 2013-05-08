package com.hlewis.eventfire.app.support

import akka.actor.{ActorRef, ReceiveTimeout, Actor}
import scala.concurrent.duration._

class PeriodicMessageSender(duration: Duration, pendingJobDispatcher: ActorRef, message: Object) extends Actor {

  context.setReceiveTimeout(duration)

  def receive = {
    case ReceiveTimeout => pendingJobDispatcher ! message
  }
}
