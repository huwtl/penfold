package com.hlewis.eventfire.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import com.hlewis.eventfire.app.support.JobJsonConverter
import com.hlewis.eventfire.app.support.hal.HalTriggeredJobFeedFormatter
import com.hlewis.eventfire.usecases.{RetrieveTriggeredJobs, RetrieveTriggeredJobsByType, RetrieveTriggeredJob}

class TriggeredJobFeedResource(retrieveTriggeredJob: RetrieveTriggeredJob,
                               retrieveTriggeredJobs: RetrieveTriggeredJobs,
                               retrieveTriggeredJobsByType: RetrieveTriggeredJobsByType,
                               jsonConverter: JobJsonConverter,
                               halFormatter: HalTriggeredJobFeedFormatter) extends ScalatraServlet {

  before() {
    contentType = HAL_JSON
  }

  get("/") {
    Ok(halFormatter.halFrom(params.get("type") match {
      case Some(restrictByJobType) => retrieveTriggeredJobsByType.retrieve(restrictByJobType)
      case _ => retrieveTriggeredJobs.retrieve()
    }))
  }

  get("/:id") {
    retrieveTriggeredJob.retrieve(params("id")) match {
      case Some(job) => Ok(halFormatter.halFrom(job))
      case _ => NotFound("Triggered job not found")
    }
  }
}
