package org.huwtl.penfold.readstore

import org.slf4j.LoggerFactory

class EventNotifier(newEventsProvider: NewEventsProvider, eventListener: EventListener) {
  private val logger = LoggerFactory.getLogger(getClass)

  def notifyListener() {
    try {
      newEventsProvider.newEvents foreach eventListener.handle
    }
    catch {
      case e: Exception => logger.error("error by listener while handling events", e)
    }
  }
}
