package com.hlewis.eventfire.usecases

import akka.actor.Actor

class ExposeTriggeredJobs() extends Actor  {

  override def receive = {
    case Refresh => {
      println("refresh...")
    }
  }
}

case object Refresh