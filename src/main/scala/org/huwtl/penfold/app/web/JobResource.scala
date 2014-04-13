package org.huwtl.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import org.huwtl.penfold.app.support.hal.HalJobFormatter
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.readstore.ReadStore
import org.huwtl.penfold.command.CommandDispatcher
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.app.web.bean.JobCreationRequest
import org.huwtl.penfold.app.support.auth.BasicAuthenticationSupport
import org.huwtl.penfold.app.AuthenticationCredentials

class JobResource(readStore: ReadStore,
                  commandDispatcher: CommandDispatcher,
                  jsonConverter: ObjectSerializer,
                  halFormatter: HalJobFormatter,
                  authenticationCredentials: Option[AuthenticationCredentials]) extends ScalatraServlet with FilterParamsProvider with PageRequestProvider with ErrorHandling with BasicAuthenticationSupport {

  private val pageSize = 5

  before() {
    contentType = HAL_JSON
  }

  get("/:id") {
    readStore.retrieveBy(AggregateId(params("id"))) match {
      case Some(job) => Ok(halFormatter.halFrom(job))
      case _ => errorResponse(NotFound("Job not found"))
    }
  }

  get("/") {
    val filters = parseFilters(params)
    val page = parsePageRequestParams(params, pageSize)
    Ok(halFormatter.halFrom(page, readStore.retrieveBy(filters, page), filters))
  }

  post("/") {
    val createJobRequest = jsonConverter.deserialize[JobCreationRequest](request.body)
    val aggregateId = commandDispatcher.dispatch(createJobRequest.toCommand)
    Created(halFormatter.halFrom(readStore.retrieveBy(aggregateId).get))
  }

  override protected def validCredentials: Option[AuthenticationCredentials] = authenticationCredentials
}
