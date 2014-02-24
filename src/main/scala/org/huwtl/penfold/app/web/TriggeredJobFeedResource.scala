package org.huwtl.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import org.huwtl.penfold.app.support.hal.HalTriggeredJobFeedFormatter
import org.huwtl.penfold.domain.model.{Status, Id, QueueName}
import org.huwtl.penfold.query.QueryRepository

class TriggeredJobFeedResource(queryRepository: QueryRepository,
                               halFormatter: HalTriggeredJobFeedFormatter) extends ScalatraServlet {

  before() {
    contentType = HAL_JSON
  }

  get("/") {
    Ok(halFormatter.halFrom(params.get("type") match {
      case Some(restrictByQueueName) => queryRepository.retrieveBy(Status.Triggered, QueueName(restrictByQueueName))
      case _ => queryRepository.retrieveBy(Status.Triggered)
    }))
  }

  get("/:id") {
    queryRepository.retrieveBy(Id(params("id"))) match {
      case Some(job) => Ok(halFormatter.halFrom(job))
      case _ => NotFound("Triggered job not found")
    }
  }
}
