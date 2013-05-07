package com.hlewis.eventfire.app

import org.scalatra._
import scalate.ScalateSupport
import com.hlewis.eventfire.domain._
import com.hlewis.eventfire.domain.Body
import com.hlewis.eventfire.domain.Header
import com.hlewis.eventfire.domain.Job

class JobstoreController (jobstore: JobStore) extends ScalatraServlet with ScalateSupport {

  get("/") {
    ""
  }

  get("/ping") {
    "pong"
  }

  get("/redis-hash-test/:key/:value") {
    jobstore.add(Job(Header(params("key"), "test", Cron("1", "10", "*", "*", "*"), Map()), Body(Map("data" -> params("value")))))
    "added to hash"
  }

  notFound {
    findTemplate(requestPath) map {
      path =>
        contentType = "text/html"
        layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound()
  }
}
