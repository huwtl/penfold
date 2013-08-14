package com.hlewis.eventfire.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import com.hlewis.eventfire.app.support.StartJobRequestJsonConverter
import com.hlewis.eventfire.app.support.hal.HalStartedJobFormatter
import com.hlewis.eventfire.usecases.{RetrieveStartedJobs, RetrieveStartedJob, StartJob}

class StartedJobFeedResource(startJob: StartJob, retrieveStartedJob: RetrieveStartedJob, retrieveStartedJobs: RetrieveStartedJobs, jsonConverter: StartJobRequestJsonConverter, halFormatter: HalStartedJobFormatter) extends ScalatraServlet {

  before() {
    contentType = HAL_JSON
  }

  get("/") {
    Ok(halFormatter.halFrom(retrieveStartedJobs.retrieve()))
  }

  get("/:id") {
    retrieveStartedJob.retrieve(params("id")) match {
      case Some(job) => Ok(halFormatter.halFrom(job))
      case _ => NotFound("Started job not found")
    }
  }

  post("/") {
    val startJobRequest = jsonConverter.from(request.body)
    Created(halFormatter.halFrom(startJob.start(startJobRequest)))
  }
}
