package org.huwtl.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import org.huwtl.penfold.app.support.hal.HalStartedJobFormatter
import org.huwtl.penfold.domain.model.{Status, Id}
import org.huwtl.penfold.query.QueryRepository
import org.huwtl.penfold.command.{StartJob, CommandDispatcher}
import org.huwtl.penfold.app.support.json.ObjectSerializer

class StartedJobFeedResource(commandDispatcher: CommandDispatcher,
                             queryRepository: QueryRepository,
                             jsonConverter: ObjectSerializer,
                             halFormatter: HalStartedJobFormatter) extends ScalatraServlet {

  before() {
    contentType = HAL_JSON
  }

  get("/") {
    Ok(halFormatter.halFrom(queryRepository.retrieveBy(Status.Started)))
  }

  get("/:id") {
    queryRepository.retrieveBy(Id(params("id"))) match {
      case Some(job) => Ok(halFormatter.halFrom(job))
      case _ => NotFound("Started job not found")
    }
  }

  post("/") {
    val startJobCommand = jsonConverter.deserialize[StartJob](request.body)
    commandDispatcher.dispatch[StartJob](startJobCommand)
    Created(halFormatter.halFrom(queryRepository.retrieveBy(startJobCommand.id).get))
  }
}
