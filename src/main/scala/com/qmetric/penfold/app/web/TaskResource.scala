package com.qmetric.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import com.qmetric.penfold.app.support.hal.HalTaskFormatter
import com.qmetric.penfold.domain.model.{AggregateVersion, AggregateId}
import com.qmetric.penfold.readstore.ReadStore
import com.qmetric.penfold.command.CommandDispatcher
import com.qmetric.penfold.app.support.json.ObjectSerializer
import com.qmetric.penfold.app.web.bean.{UpdateTaskPayloadRequest, CreateTaskRequest}
import com.qmetric.penfold.app.support.auth.BasicAuthenticationSupport
import com.qmetric.penfold.app.AuthenticationCredentials

class TaskResource(readStore: ReadStore,
                  commandDispatcher: CommandDispatcher,
                  jsonConverter: ObjectSerializer,
                  halFormatter: HalTaskFormatter,
                  pageSize: Int,
                  authenticationCredentials: Option[AuthenticationCredentials]) extends ScalatraServlet with FilterParamsProvider with PageRequestProvider with ErrorHandling with BasicAuthenticationSupport {

  before() {
    contentType = HAL_JSON
  }

  get("/:id") {
    readStore.retrieveBy(AggregateId(params("id"))) match {
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
    val createTaskRequest = jsonConverter.deserialize[CreateTaskRequest](request.body)
    val aggregateId = commandDispatcher.dispatch(createTaskRequest.toCommand)
    Created(halFormatter.halFrom(readStore.retrieveBy(aggregateId).get))
  }

  put("/:id/:version/payload") {
    val updatePayloadTaskRequest = jsonConverter.deserialize[UpdateTaskPayloadRequest](request.body)
    val aggregateId = commandDispatcher.dispatch(updatePayloadTaskRequest.toCommand(AggregateId(params("id")), AggregateVersion(params("version").toInt)))
    Ok(halFormatter.halFrom(readStore.retrieveBy(aggregateId).get))
  }

  override protected def validCredentials: Option[AuthenticationCredentials] = authenticationCredentials
}
