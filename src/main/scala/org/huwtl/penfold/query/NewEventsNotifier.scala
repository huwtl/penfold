package org.huwtl.penfold.query

import org.slf4j.LoggerFactory

class NewEventsNotifier(newEventsProvider: NewEventsProvider, eventListener: EventListener) {
  private val logger =  LoggerFactory.getLogger(getClass)

  def notifyListener() {
    newEventsProvider.newEvents foreach {
      newEvent =>
        try {
          eventListener.handle(newEvent)
        }
        catch {
          case e: Exception => logger.error("error handling events", e)
        }
    }
  }
}
