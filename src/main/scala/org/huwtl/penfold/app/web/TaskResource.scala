package org.huwtl.penfold.app.web

import org.huwtl.penfold.app.AuthenticationCredentials
import org.huwtl.penfold.app.support.auth.BasicAuthenticationSupport
import org.huwtl.penfold.app.support.hal.HalTaskFormatter
import org.huwtl.penfold.command.CommandDispatcher
import org.huwtl.penfold.domain.model.{AggregateId, AggregateVersion}
import org.huwtl.penfold.readstore.ReadStore
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import org.scalatra._

class TaskResource(readStore: ReadStore,
                   commandDispatcher: CommandDispatcher,
                   commandParser: TaskCommandParser,
                   halFormatter: HalTaskFormatter,
                   pageSize: Int,
                   authenticationCredentials: Option[AuthenticationCredentials]) extends ScalatraServlet with FilterParamsProvider with PageRequestProvider with ErrorHandling with BasicAuthenticationSupport with RequestLogging {
  before() {
    contentType = HAL_JSON
  }

  get("/:id") {
    readStore.retrieveBy(idParamValue) match {
      case Some(task) => Ok(halFormatter.halFrom(task))
      case None => errorResponse(NotFound("Task not found"))
    }
  }

  get("/") {
    val filters = parseFilters(multiParams)
    val page = parsePageRequestParams(params, pageSize)
    Ok(halFormatter.halFrom(page, readStore.retrieveBy(filters, page), filters))
  }

  post("/") {
    val command = commandParser.parse(commandTypeFromRequest, request.body)
    val aggregateId = commandDispatcher.dispatch(command)
    Created(viewOfTask(aggregateId))
  }

  post("/:id/:version") {
    val command = commandParser.parse(commandTypeFromRequest, idParamValue, versionParamValue, request.body)
    val aggregateId = commandDispatcher.dispatch(command)
    Ok(viewOfTask(aggregateId))
  }

  private def commandTypeFromRequest = ContentTypeWithCommandType(request.contentType).extractedCommandType

  private def viewOfTask(id: AggregateId) = halFormatter.halFrom(readStore.retrieveBy(id).get)

  private def idParamValue = AggregateId(params("id"))

  private def versionParamValue = AggregateVersion(params("version").toInt)

  override protected def validCredentials: Option[AuthenticationCredentials] = authenticationCredentials
}
