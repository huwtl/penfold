package org.huwtl.penfold.app.web

import grizzled.slf4j.Logger
import org.scalatra._

trait RequestLogging extends ScalatraServlet {
  private lazy val logger = Logger(getClass)

  before() {
    logger.info(s"$logMessage - started")
  }

  after() {
    logger.info(s"$logMessage - finished")
  }

  private def logMessage: String =
  {
    val queryString = Option(request.getQueryString) match {
      case None => ""
      case Some(q) => s"?$q"
    }

    s"${request.getMethod}: ${request.getRequestURL}${queryString}"
  }
}
