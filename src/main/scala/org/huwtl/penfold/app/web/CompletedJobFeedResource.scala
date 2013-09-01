package org.huwtl.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import org.huwtl.penfold.app.support.CompleteJobRequestJsonConverter
import org.huwtl.penfold.app.support.hal.HalCompletedJobFormatter
import org.huwtl.penfold.usecases.{RetrieveCompletedJobs, RetrieveCompletedJob, CompleteJob}
import org.huwtl.penfold.domain.Id

class CompletedJobFeedResource(completeStartedJob: CompleteJob, retrieveCompletedJob: RetrieveCompletedJob, retrieveCompletedJobs: RetrieveCompletedJobs, jsonConverter: CompleteJobRequestJsonConverter, halFormatter: HalCompletedJobFormatter) extends ScalatraServlet {

  before() {
    contentType = HAL_JSON
  }

  get("/") {
    Ok(halFormatter.halFrom(retrieveCompletedJobs.retrieve()))
  }

  get("/:id") {
    retrieveCompletedJob.retrieve(Id(params("id"))) match {
      case Some(job) => Ok(halFormatter.halFrom(job))
      case _ => NotFound("Completed job not found")
    }
  }

  post("/") {
    val completeJobRequest = jsonConverter.from(request.body)
    Created(halFormatter.halFrom(completeStartedJob.complete(completeJobRequest)))
  }
}
