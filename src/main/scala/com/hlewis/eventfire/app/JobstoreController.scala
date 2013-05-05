package com.hlewis.eventfire.app

import org.scalatra._
import exchange.RabbitJobExchange
import scalate.ScalateSupport
import com.hlewis.eventfire.domain.JobStore

class JobstoreController (jobstore: JobStore, queue: RabbitJobExchange) extends ScalatraServlet with ScalateSupport {

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
