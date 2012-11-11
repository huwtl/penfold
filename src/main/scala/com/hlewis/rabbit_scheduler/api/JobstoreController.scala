package com.hlewis.rabbit_scheduler.api

import org.scalatra._
import scalate.ScalateSupport
import com.hlewis.rabbit_scheduler.jobstore.{Jobstore, RedisJobstore}
import com.google.inject.Inject
import com.hlewis.rabbit_scheduler.queue.RabbitQueue

class JobstoreController @Inject()(val jobstore: Jobstore, val queue: RabbitQueue) extends ScalatraServlet with ScalateSupport {

  get("/ping") {
    "pong"
  }

  get("/redis-hash-test/:key/:value") {
    jobstore.add(params("key"), params("value"))
    "added to hash"
  }

  get("/rabbit-test") {
    queue.send()

    "rabbit test comple"
  }

  notFound {
    findTemplate(requestPath) map {
      path =>
        contentType = "text/html"
        layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound()
  }
}
