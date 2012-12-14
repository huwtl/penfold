package com.hlewis.rabbit_scheduler.domain

import org.slf4j.LoggerFactory
import actors.{TIMEOUT, Actor}

class Consumer(f: (Unit => Any)) extends Actor {
  val LOGGER = LoggerFactory.getLogger(getClass)

  def act() {
    loop {
      react {
        case Ping => {
          f()
        }
      }
    }
  }

  override def exceptionHandler = {
    case e: Exception => {
      LOGGER.error("Exception: ", e)
    }
  }
}

class ConsumerTrigger(timeout: Long, who: Actor) extends Actor {
  override def act() {
    loop {
      reactWithin(timeout) {
        case TIMEOUT => who ! Ping
      }
    }
  }
}

case object Ping

object StartActor {

  def start() = {
    val a = new Consumer(_ => println("ping"))


    val c = new ConsumerTrigger(1000, a)

    a.start()
    c.start()
  }
}