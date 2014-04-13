package org.huwtl.penfold.readstore

import grizzled.slf4j.Logger
import scala.util.Try

class EventNotifier(newEventsProvider: NewEventsProvider, eventListener: EventListener) {
  private lazy val logger = Logger(getClass)

  def notifyListener() {
    Try(newEventsProvider.newEvents foreach eventListener.handle) recover {
      case e: Exception => logger.error("error by listener while handling events", e)
    }
  }
}
