package org.huwtl.penfold.app.web

import org.scalatra.test.specs2.MutableScalatraSpec
import org.specs2.mock.Mockito
import com.codahale.metrics.health.HealthCheckRegistry
import org.huwtl.penfold.app.support.json.ObjectSerializer

class HealthCheckResourceTest extends MutableScalatraSpec with Mockito {
  sequential

  addServlet(new HealthCheckResource(new HealthCheckRegistry, new ObjectSerializer), "/healthcheck")

  "return 200" in {
    get("/healthcheck") {
      status must beEqualTo(200)
    }
  }
}
