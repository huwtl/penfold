package com.hlewis.eventfire.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import com.hlewis.eventfire.app.support.CompleteJobRequestJsonConverter
import com.hlewis.eventfire.app.support.hal.HalCompletedJobFormatter
import com.hlewis.eventfire.usecases.CompleteJob

class CompletedJobFeedResource(completeStartedJob: CompleteJob, jsonConverter: CompleteJobRequestJsonConverter, halFormatter: HalCompletedJobFormatter) extends ScalatraServlet {

  before() {
    contentType = HAL_JSON
  }

  post("/") {
    val completeJobRequest = jsonConverter.from(request.body)
    completeStartedJob.complete(completeJobRequest) match {
      case Some(completedJob) => Created(halFormatter.halFrom(completedJob))
      case _ => NotFound("Job not found to complete")
    }
  }
}
