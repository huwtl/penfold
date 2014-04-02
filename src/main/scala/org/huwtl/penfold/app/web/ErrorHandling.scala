package org.huwtl.penfold.app.web

import org.scalatra._
import org.huwtl.penfold.domain.exceptions.EventConflictException
import org.scalatra.ActionResult
import org.slf4j.LoggerFactory
import org.json4s.MappingException

trait ErrorHandling extends ScalatraServlet {
  private val logger =  LoggerFactory.getLogger(getClass)
  
  private val errorContentType = "text/plain"

  error {
    case e: IllegalArgumentException => errorResponse(BadRequest(s"Bad request: ${e.getMessage}"), e)
    case e: IllegalStateException => errorResponse(BadRequest(s"Bad request: ${e.getMessage}"), e)
    case e: MappingException => errorResponse(BadRequest(s"Bad request: ${e.getMessage}"), e)
    case e: EventConflictException => {
      logger.info("Conflict:", e)
      errorResponse(Conflict(s"Conflict: ${e.getMessage}"))
    }
    case e: Exception => errorResponse(InternalServerError("Crumbs!"), e)
  }

  def errorResponse(errorResp: ActionResult, e: Exception): ActionResult = {
    logger.error("Error:", e)
    errorResponse(errorResp)
  }
  
  def errorResponse(errorResp: ActionResult): ActionResult = {
    contentType = errorContentType
    errorResp
  }
}
