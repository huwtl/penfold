package org.huwtl.penfold.readstore

import grizzled.slf4j.Logger

class EventNotifiersImpl(notifiers: List[EventNotifier]) extends EventNotifiers {
  private lazy val logger = Logger(getClass)

  override def notifyAllOfEvents() {
    logger.info("new events notification starting")
    notifiers.foreach(_.notifyListener())
    logger.info("new events notification completed")
  }
}
