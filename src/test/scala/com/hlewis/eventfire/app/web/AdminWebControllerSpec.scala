package com.hlewis.eventfire.app.web

import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.FunSpec
import org.scalatest.mock.MockitoSugar
import com.hlewis.eventfire.domain.JobStore

class AdminWebControllerSpec extends ScalatraSuite with FunSpec with MockitoSugar {

  val jobstore = mock[JobStore]

  addServlet(new AdminWebController(jobstore), "/*")

  describe("Ping request") {

    it("should return 200") {
      get("/ping") {
        status should equal(200)
        body should include("pong")
      }
    }

  }
}
