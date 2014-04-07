package org.huwtl.penfold.app.support.metrics

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result._
import com.codahale.metrics.health.HealthCheck.Result
import org.huwtl.penfold.readstore.ReadStore

class ReadStoreConnectivityHealthcheck(readStore: ReadStore) extends HealthCheck {
  override def check(): Result = {
    readStore.checkConnectivity match {
      case Left(true) => healthy
      case Left(false) => unhealthy("failed to connect to read store")
      case Right(exception) => unhealthy(exception)
    }
  }
}
