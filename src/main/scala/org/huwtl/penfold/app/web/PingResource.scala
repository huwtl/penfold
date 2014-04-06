package org.huwtl.penfold.app.web

import org.scalatra._

class PingResource extends ScalatraServlet with ErrorHandling {
  get("/") {
    Ok("hello")
  }
}
