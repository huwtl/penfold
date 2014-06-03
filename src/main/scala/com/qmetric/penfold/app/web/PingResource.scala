package com.qmetric.penfold.app.web

import org.scalatra._

class PingResource extends ScalatraServlet with ErrorHandling {
  get("/") {
    Ok("hello")
  }
}
