package org.huwtl.penfold.app.web

import org.scalatra._

class PingResource extends ScalatraServlet {
  get("/") {
    Ok("pong")
  }
}
