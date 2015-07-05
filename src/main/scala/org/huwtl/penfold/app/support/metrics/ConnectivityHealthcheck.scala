package org.huwtl.penfold.app.support.metrics

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result
import com.codahale.metrics.health.HealthCheck.Result._
import org.huwtl.penfold.app.support.ConnectivityCheck

class ConnectivityHealthcheck(readStore: ConnectivityCheck) extends HealthCheck {
  override def check(): Result = {
    readStore.checkConnectivity match {
      case Left(true) => healthy
      case Left(false) => unhealthy("connection failed")
      case Right(exception) => unhealthy(exception)
    }
  }
}
