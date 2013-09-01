package org.huwtl.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import org.huwtl.penfold.app.support.JobJsonConverter
import org.huwtl.penfold.app.support.hal.HalJobFormatter
import org.huwtl.penfold.usecases.{RetrieveJobById, CreateJob}
import org.huwtl.penfold.domain.Id

class JobsResource(retrieveExistingJob: RetrieveJobById, createJob: CreateJob, jsonConverter: JobJsonConverter, halFormatter: HalJobFormatter) extends ScalatraServlet {

  before() {
    contentType = HAL_JSON
  }

  get("/:id") {
    retrieveExistingJob.retrieve(Id(params("id"))) match {
      case Some(job) => Ok(halFormatter.halFrom(job))
      case _ => NotFound("Job not found")
    }
  }

  post("/") {
    val newJob = createJob.create(jsonConverter.jobFrom(request.body))
    Created(halFormatter.halFrom(newJob))
  }
}
