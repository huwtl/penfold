package org.huwtl.penfold.app.support.metrics

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result._
import org.huwtl.penfold.domain.store.EventStore
import com.codahale.metrics.health.HealthCheck.Result

class EventStoreConnectivityHealthcheck(eventStore: EventStore) extends HealthCheck {
  override def check(): Result = {
    eventStore.checkConnectivity match {
      case Left(true) => healthy
      case Left(false) => unhealthy("failed to connect to event store")
      case Right(exception) => unhealthy(exception)
    }
  }
}
