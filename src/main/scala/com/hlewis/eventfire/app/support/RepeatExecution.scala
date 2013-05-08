package com.hlewis.eventfire.app.support

import akka.actor.{ReceiveTimeout, Actor}
import scala.concurrent.duration._

class RepeatExecution(repeatDelay: Duration, func: => Unit) extends Actor {

  context.setReceiveTimeout(repeatDelay)

  def receive = {
    case ReceiveTimeout => func
  }
}
