package org.huwtl.penfold.query

import org.slf4j.LoggerFactory

class EventNotifier(newEventsProvider: NewEventsProvider, eventListener: EventListener) {
  private val logger =  LoggerFactory.getLogger(getClass)

  def notifyListener() {
    newEventsProvider.newEvents foreach {
      event =>
        try {
          eventListener.handle(event)
        }
        catch {
          case e: Exception => logger.error("error handling events", e)
        }
    }
  }
}
