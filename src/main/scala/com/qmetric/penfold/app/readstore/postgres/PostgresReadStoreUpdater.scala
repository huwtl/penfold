package com.qmetric.penfold.app.readstore.postgres

import com.qmetric.penfold.app.readstore.postgres.subscribers.Subscribers
import com.qmetric.penfold.app.support.json.ObjectSerializer
import com.qmetric.penfold.domain.event._
import com.qmetric.penfold.readstore.EventListener
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
