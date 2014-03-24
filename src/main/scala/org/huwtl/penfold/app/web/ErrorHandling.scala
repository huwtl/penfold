package org.huwtl.penfold.app.web

import org.scalatra.{ScalatraServlet, InternalServerError, BadRequest}
import org.huwtl.penfold.domain.exceptions.EventConflictException

trait ErrorHandling extends ScalatraServlet {
  val errorContentType = "text/plain"

  error {
    case e: IllegalArgumentException => {
      contentType = errorContentType
      BadRequest(s"Bad request: ${e.getMessage}")
    }
    case e: EventConflictException => {
      contentType = errorContentType
      InternalServerError(s"Conflict: ${e.getMessage}")
    }
    case e: Exception => {
      contentType = errorContentType
      InternalServerError("Server error")
    }
  }
}
