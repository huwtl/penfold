package org.huwtl.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import org.huwtl.penfold.app.support.hal.HalCompletedJobFormatter
import org.huwtl.penfold.domain.model.{Status, Id}
import org.huwtl.penfold.query.QueryRepository
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.command.{CommandDispatcher, CompleteJob}

class CompletedJobFeedResource(queryRepository: QueryRepository, commandDispatcher: CommandDispatcher, jsonConverter: ObjectSerializer, halFormatter: HalCompletedJobFormatter) extends ScalatraServlet {

  before() {
    contentType = HAL_JSON
  }

  get("/") {
    Ok(halFormatter.halFrom(queryRepository.retrieveBy(Status.Completed)))
  }

  get("/:id") {
    queryRepository.retrieveBy(Id(params("id"))) match {
      case Some(job) => Ok(halFormatter.halFrom(job))
      case _ => NotFound("Completed job not found")
    }
  }

  post("/") {
    val completeJobCommand = jsonConverter.deserialize[CompleteJob](request.body)
    commandDispatcher.dispatch[CompleteJob](completeJobCommand)
    Created(halFormatter.halFrom(queryRepository.retrieveBy(completeJobCommand.id)))
  }
}
