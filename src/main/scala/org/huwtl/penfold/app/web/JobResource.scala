package org.huwtl.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import org.huwtl.penfold.app.support.hal.HalJobFormatter
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.query.{PageRequest, QueryRepository}
import org.huwtl.penfold.command.CommandDispatcher
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.app.web.bean.JobCreationRequest

class JobResource(queryRepository: QueryRepository, commandDispatcher: CommandDispatcher, jsonConverter: ObjectSerializer, halFormatter: HalJobFormatter) extends ScalatraServlet with FilterParamsProvider with ErrorHandling {

  private val pageSize = 10

  before() {
    contentType = HAL_JSON
  }

  get("/:id") {
    queryRepository.retrieveBy(AggregateId(params("id"))) match {
      case Some(job) => Ok(halFormatter.halFrom(job))
      case _ => errorResponse(NotFound("Job not found"))
    }
  }

  get("/") {
    val filters = parseFilters(params)
    val page = PageRequest(params.getOrElse("page", "0").toInt, pageSize)
    Ok(halFormatter.halFrom(queryRepository.retrieveBy(filters, page), filters))
  }

  post("/") {
    val createJobRequest = jsonConverter.deserialize[JobCreationRequest](request.body)
    val aggregateId = commandDispatcher.dispatch(createJobRequest.toCommand)
    Created(halFormatter.halFrom(queryRepository.retrieveBy(aggregateId).get))
  }
}
