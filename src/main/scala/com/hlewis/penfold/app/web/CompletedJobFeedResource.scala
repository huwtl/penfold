package com.hlewis.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import com.hlewis.penfold.app.support.CompleteJobRequestJsonConverter
import com.hlewis.penfold.app.support.hal.HalCompletedJobFormatter
import com.hlewis.penfold.usecases.{RetrieveCompletedJobs, RetrieveCompletedJob, CompleteJob}

class CompletedJobFeedResource(completeStartedJob: CompleteJob, retrieveCompletedJob: RetrieveCompletedJob, retrieveCompletedJobs: RetrieveCompletedJobs, jsonConverter: CompleteJobRequestJsonConverter, halFormatter: HalCompletedJobFormatter) extends ScalatraServlet {

  before() {
    contentType = HAL_JSON
  }

  get("/") {
    Ok(halFormatter.halFrom(retrieveCompletedJobs.retrieve()))
  }

  get("/:id") {
    retrieveCompletedJob.retrieve(params("id")) match {
      case Some(job) => Ok(halFormatter.halFrom(job))
      case _ => NotFound("Completed job not found")
    }
  }

  post("/") {
    val completeJobRequest = jsonConverter.from(request.body)
    Created(halFormatter.halFrom(completeStartedJob.complete(completeJobRequest)))
  }
}
