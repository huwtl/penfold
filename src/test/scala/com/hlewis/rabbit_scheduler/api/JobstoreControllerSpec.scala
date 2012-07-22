package com.hlewis.rabbit_scheduler.api

import org.scalatra.test.specs2._
import com.hlewis.rabbit_scheduler.jobstore.RedisJobstore
import org.specs2.mock.Mockito

class JobstoreControllerSpec extends ScalatraSpec with Mockito {

  val jobstore = mock[RedisJobstore]

  addServlet(new JobstoreController(jobstore), "/*")

  def is =
  "GET / on JobstoreController" ^
  "should return status 200" ! root200

  def root200 = get("/") {
    status must_== 200
  }
}
