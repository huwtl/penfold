package com.hlewis.eventfire.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import com.hlewis.eventfire.app.support.CompleteJobRequestJsonConverter
import com.hlewis.eventfire.app.support.hal.HalCompletedJobFormatter
import com.hlewis.eventfire.usecases.{RetrieveCompletedJobs, RetrieveCompletedJob, CompleteJob}

class PingResource extends ScalatraServlet {
  get("/") {
    Ok("pong")
  }
}
