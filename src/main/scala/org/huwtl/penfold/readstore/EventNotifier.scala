package org.huwtl.penfold.readstore

import grizzled.slf4j.Logger

class EventNotifier(newEventsProvider: NewEventsProvider, eventListener: EventListener) {
  private lazy val logger = Logger(getClass)

  def notifyListener() {
    try {
      newEventsProvider.newEvents foreach eventListener.handle
    }
    catch {
      case e: Exception => logger.error("error by listener while handling events", e)
    }
  }
}
