package org.huwtl.penfold.app.readstore.postgres

import org.huwtl.penfold.app.readstore.postgres.subscribers.Subscribers
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.event._
import org.huwtl.penfold.readstore.EventListener
import grizzled.slf4j.Logger

class PostgresReadStoreUpdater(objectSerializer: ObjectSerializer) extends EventListener {
  private lazy val logger = Logger(getClass)

  private val subscribers = new Subscribers

  override def handle(event: Event) {
    subscribers.findSuitable(event) match {
      case Some(subscriber) => {
        subscriber.handleEvent(event, objectSerializer)
        logger.info(s"event $event handled")
      }
      case _ => logger.warn(s"no handler found for event $event")
    }
  }
}
