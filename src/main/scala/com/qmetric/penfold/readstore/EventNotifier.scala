package com.qmetric.penfold.readstore

import com.qmetric.penfold.support.Retry.retry
import grizzled.slf4j.Logger

class EventNotifier(newEventsProvider: NewEventsProvider, eventListener: EventListener, maxRetries: Int = 10) {
  private lazy val logger = Logger(getClass)

  def notifyListener() {
    retry(maxRetries) {
      newEventsProvider.newEvents foreach {event =>
        logger.info(s"handling event ${event}")
        eventListener.handle(event)
      }
    } recover {
      case e => logger.error("error by listener while handling events", e)
    }
  }
}
