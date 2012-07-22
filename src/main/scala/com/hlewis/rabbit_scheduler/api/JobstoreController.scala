package com.hlewis.rabbit_scheduler.api

import org.scalatra._
import scalate.ScalateSupport
import com.hlewis.rabbit_scheduler.jobstore.RedisJobstore

class JobstoreController(jobstore: RedisJobstore = new RedisJobstore()) extends ScalatraServlet with ScalateSupport {

  get("/") {
    "pong"
  }

  get("/redis-hash-test/:key/:value") {
    jobstore.add(params("key"), params("value"))
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
