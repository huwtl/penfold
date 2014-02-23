package org.huwtl.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import org.huwtl.penfold.app.support.hal.HalJobFormatter
import org.huwtl.penfold.domain.model.Id
import org.huwtl.penfold.query.QueryRepository
import org.huwtl.penfold.command.{CreateJob, CommandDispatcher}
import org.huwtl.penfold.app.support.json.ObjectSerializer

class JobsResource(queryRepository: QueryRepository, commandDispatcher: CommandDispatcher, jsonConverter: ObjectSerializer, halFormatter: HalJobFormatter) extends ScalatraServlet {

  before() {
    contentType = HAL_JSON
  }

  get("/:id") {
    queryRepository.retrieveBy(Id(params("id"))) match {
      case Some(job) => Ok(halFormatter.halFrom(job))
      case _ => NotFound("Job not found")
    }
  }

  post("/") {
    val createJobCommand = jsonConverter.deserialize[CreateJob](request.body)
    commandDispatcher.dispatch(createJobCommand)
    Created(halFormatter.halFrom(queryRepository.retrieveBy(createJobCommand.id).get))
  }
}
