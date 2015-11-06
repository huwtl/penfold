package com.qmetric.penfold.app.web

import com.codahale.metrics.health.HealthCheckRegistry
import com.qmetric.penfold.app.support.json.ObjectSerializer
import org.scalatra.test.specs2.MutableScalatraSpec
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationWithJUnit

class HealthResourceTest extends SpecificationWithJUnit with MutableScalatraSpec with Mockito {
  sequential

  addServlet(new HealthResource(new HealthCheckRegistry, new ObjectSerializer), "/healthcheck")

  "return 200" in {
    get("/healthcheck") {
      status must beEqualTo(200)
    }
  }
}
