package com.hlewis.rabbit_scheduler.app

import actors.{TIMEOUT, Actor}

class PeriodicPendingJobDispatchTrigger(period: Long, pendingJobDispatcher: Actor) extends Actor {
  override def act() {
    loop {
      reactWithin(period) {
        case TIMEOUT => pendingJobDispatcher ! DispatchPending
      }
    }
  }
}
