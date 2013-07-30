package com.hlewis.eventfire.app.support.hal

import com.theoryinpractise.halbuilder.api.RepresentationFactory
import com.theoryinpractise.halbuilder.api.RepresentationFactory._
import com.hlewis.eventfire.domain.Job
import java.net.URI

class HalStartedJobFormatter(representationFactory: RepresentationFactory, selfLink: URI, jobLink: URI, completedJobLink: URI) {

  def halFrom(job: Job) = {
    representationFactory.newRepresentation(s"${selfLink.toString}/${job.id}")
      .withProperty("jobId", job.id)
      .withLink("job", s"${jobLink.toString}/${job.id}")
      .withLink("complete", s"${completedJobLink.toString}")
      .toString(HAL_JSON)
  }
}
