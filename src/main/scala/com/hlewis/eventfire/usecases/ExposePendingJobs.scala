package com.hlewis.eventfire.usecases

import akka.actor.Actor

class ExposePendingJobs() extends Actor  {

  override def receive = {
    case Refresh => {
      println("refresh...")
    }
  }
}

case object Refresh