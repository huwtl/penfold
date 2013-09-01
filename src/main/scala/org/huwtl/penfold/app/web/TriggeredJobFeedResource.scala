package org.huwtl.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import org.huwtl.penfold.app.support.JobJsonConverter
import org.huwtl.penfold.app.support.hal.HalTriggeredJobFeedFormatter
import org.huwtl.penfold.usecases.{RetrieveTriggeredJobs, RetrieveTriggeredJobsByType, RetrieveTriggeredJob}
import org.huwtl.penfold.domain.{Id, JobType}

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
      case Some(restrictByJobType) => retrieveTriggeredJobsByType.retrieve(JobType(restrictByJobType))
      case _ => retrieveTriggeredJobs.retrieve()
    }))
  }

  get("/:id") {
    retrieveTriggeredJob.retrieveBy(Id(params("id"))) match {
      case Some(job) => Ok(halFormatter.halFrom(job))
      case _ => NotFound("Triggered job not found")
    }
  }
}
