package com.hlewis.rabbit_scheduler.app

import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.FunSpec
import org.scalatest.mock.MockitoSugar

class JobstoreControllerSpec extends ScalatraSuite with FunSpec with MockitoSugar {

  val jobstore = mock[RedisJobStore]

  addServlet(new JobstoreController(jobstore, null), "/*")

  describe("Ping request") {

    it("should return 200") {
      get("/ping") {
        status should equal(200)
        body should include("pong")
      }
    }

  }
}
