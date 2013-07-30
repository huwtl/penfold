package com.hlewis.eventfire.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import com.hlewis.eventfire.app.support.JobJsonConverter
import com.hlewis.eventfire.app.support.hal.HalJobFormatter
import com.hlewis.eventfire.usecases.{RetrieveJob, CreateJob}

class JobsResource(retrieveExistingJob: RetrieveJob, createJob: CreateJob, jsonConverter: JobJsonConverter, halFormatter: HalJobFormatter) extends ScalatraServlet {

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
