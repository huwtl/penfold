package com.hlewis.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import com.hlewis.penfold.app.support.JobJsonConverter
import com.hlewis.penfold.app.support.hal.HalJobFormatter
import com.hlewis.penfold.usecases.{RetrieveJobById, CreateJob}

class JobsResource(retrieveExistingJob: RetrieveJobById, createJob: CreateJob, jsonConverter: JobJsonConverter, halFormatter: HalJobFormatter) extends ScalatraServlet {

  before() {
    contentType = HAL_JSON
  }

  get("/:id") {
    retrieveExistingJob.retrieve(params("id")) match {
      case Some(job) => Ok(halFormatter.halFrom(job))
      case _ => NotFound("Job not found")
    }
  }

  post("/") {
    val newJob = createJob.create(jsonConverter.jobFrom(request.body))
    Created(halFormatter.halFrom(newJob))
  }
}
