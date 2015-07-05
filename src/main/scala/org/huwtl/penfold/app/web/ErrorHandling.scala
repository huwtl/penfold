package org.huwtl.penfold.app.web

import org.scalatra._
import org.huwtl.penfold.domain.exceptions.AggregateConflictException
import org.scalatra.ActionResult
import org.json4s.MappingException
import com.fasterxml.jackson.core.JsonParseException
import grizzled.slf4j.Logger

trait ErrorHandling extends ScalatraServlet {
  private lazy val logger = Logger(getClass)

  private val errorContentType = "text/plain"

  error {
    case e: IllegalArgumentException => errorResponse(BadRequest(s"Bad request: ${e.getMessage}"), e)
    case e: IllegalStateException => errorResponse(BadRequest(s"Bad request: ${e.getMessage}"), e)
    case e: MappingException => errorResponse(BadRequest(s"Bad request: ${e.getMessage}"), e)
    case e: JsonParseException => errorResponse(BadRequest(s"Bad request: ${e.getMessage}"), e)
    case e: AggregateConflictException => {
      val conflict = Conflict(e.getMessage)
      logger.info(s"${conflict.status.line}:", e)
      errorResponse(conflict)
    }
    case e: Exception => errorResponse(InternalServerError("Crumbs!"), e)
  }

  def errorResponse(errorResp: ActionResult, e: Exception): ActionResult = {
    logger.error(s"${errorResp.status.line}:", e)
    errorResponse(errorResp)
  }

  def errorResponse(errorResp: ActionResult): ActionResult = {
    contentType = errorContentType
    errorResp
  }
}
