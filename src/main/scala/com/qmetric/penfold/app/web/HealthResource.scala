package com.qmetric.penfold.app.web

import org.scalatra._
import com.codahale.metrics.health.HealthCheckRegistry
import scala.collection.JavaConversions._
import com.qmetric.penfold.app.support.json.ObjectSerializer
import scala.collection.mutable
import com.codahale.metrics.health.HealthCheck.Result

class HealthResource(healthCheckRegistry: HealthCheckRegistry, objectSerializer: ObjectSerializer) extends ScalatraServlet {
  before() {
    contentType = "application/json"
  }

  get("/") {
    val healthCheckResults = mapAsScalaMap(healthCheckRegistry.runHealthChecks())

    if (healthCheckResults.values.exists(!_.isHealthy)) {
      InternalServerError(serializeHealthCheckResults(healthCheckResults))
    }
    else {
      Ok(serializeHealthCheckResults(healthCheckResults))
    }
  }

  private def serializeHealthCheckResults(healthCheckResults: mutable.Map[String, Result]) = {
    val immutableMap: Map[String, Result] = Map(healthCheckResults.toSeq: _*)
    val resultsMap = immutableMap.mapValues(result => HealthCheckResult(result.isHealthy, Option(result.getMessage)))

    objectSerializer.serialize[Map[String, HealthCheckResult]](resultsMap)
  }
}

case class HealthCheckResult(healthy: Boolean, message: Option[String])