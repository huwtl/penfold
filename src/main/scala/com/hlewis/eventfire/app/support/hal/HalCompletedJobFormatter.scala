package com.hlewis.eventfire.app.support.hal

import com.theoryinpractise.halbuilder.api.RepresentationFactory
import com.theoryinpractise.halbuilder.api.RepresentationFactory._
import com.hlewis.eventfire.domain.Job
import java.net.URI

class HalCompletedJobFormatter(representationFactory: RepresentationFactory, selfLink: URI) {

  def halFrom(job: Job) = {
    representationFactory.newRepresentation(s"${selfLink.toString}/${job.id}")
      .withProperty("jobId", job.id)
      .toString(HAL_JSON)
  }
}
