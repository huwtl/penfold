package com.hlewis.rabbit_scheduler.api

import org.scalatra._
import scalate.ScalateSupport
import com.hlewis.rabbit_scheduler.jobstore.{Jobstore, RedisJobstore}
import com.google.inject.Inject

class JobstoreController @Inject()(val jobstore: Jobstore) extends ScalatraServlet with ScalateSupport {

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
