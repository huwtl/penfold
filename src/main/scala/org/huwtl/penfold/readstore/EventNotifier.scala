package org.huwtl.penfold.readstore

import grizzled.slf4j.Logger
import org.huwtl.penfold.domain.event.Event

class EventNotifier(eventListener: EventListener) {
  private lazy val logger = Logger(getClass)

  def notify(events: List[Event]) {
    events.foreach {event =>
      logger.info(s"handling event ${event}")
      eventListener.handle(event)
    }
  }
}
