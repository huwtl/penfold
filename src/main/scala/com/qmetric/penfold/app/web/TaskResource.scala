package com.qmetric.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import com.qmetric.penfold.app.support.hal.HalTaskFormatter
import com.qmetric.penfold.domain.model.{AggregateVersion, AggregateId}
import com.qmetric.penfold.readstore.ReadStore
import com.qmetric.penfold.command.CommandDispatcher
import com.qmetric.penfold.app.support.auth.BasicAuthenticationSupport
import com.qmetric.penfold.app.AuthenticationCredentials

class TaskResource(readStore: ReadStore,
                   commandDispatcher: CommandDispatcher,
                   commandParser: TaskCommandParser,
                   halFormatter: HalTaskFormatter,
                   pageSize: Int,
                   authenticationCredentials: Option[AuthenticationCredentials]) extends ScalatraServlet with FilterParamsProvider with PageRequestProvider with ErrorHandling with BasicAuthenticationSupport {



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

  get("/expired/:timeoutId") {
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
