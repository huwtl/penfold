package com.qmetric.penfold.app.web

import org.scalatra.test.specs2.MutableScalatraSpec
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationWithJUnit

class PingResourceTest extends SpecificationWithJUnit with MutableScalatraSpec with Mockito {
  sequential

  addServlet(new PingResource, "/ping")

  "return 200" in {
    get("/ping") {
      status must beEqualTo(200)
    }
  }
}
