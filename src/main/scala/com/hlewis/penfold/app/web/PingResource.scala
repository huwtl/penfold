package com.hlewis.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import com.hlewis.penfold.app.support.CompleteJobRequestJsonConverter
import com.hlewis.penfold.app.support.hal.HalCompletedJobFormatter
import com.hlewis.penfold.usecases.{RetrieveCompletedJobs, RetrieveCompletedJob, CompleteJob}

class PingResource extends ScalatraServlet {
  get("/") {
    Ok("pong")
  }
}
