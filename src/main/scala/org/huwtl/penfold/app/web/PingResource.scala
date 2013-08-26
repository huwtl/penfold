package org.huwtl.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import org.huwtl.penfold.app.support.CompleteJobRequestJsonConverter
import org.huwtl.penfold.app.support.hal.HalCompletedJobFormatter
import org.huwtl.penfold.usecases.{RetrieveCompletedJobs, RetrieveCompletedJob, CompleteJob}

class PingResource extends ScalatraServlet {
  get("/") {
    Ok("pong")
  }
}
