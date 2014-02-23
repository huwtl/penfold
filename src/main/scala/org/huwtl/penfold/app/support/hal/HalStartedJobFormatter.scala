package org.huwtl.penfold.app.support.hal

import com.theoryinpractise.halbuilder.api.RepresentationFactory._
import java.net.URI
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory
import org.huwtl.penfold.query.JobRecord

class HalStartedJobFormatter(selfLink: URI, jobLink: URI, completedJobLink: URI) {
  private val representationFactory = new DefaultRepresentationFactory

  def halFrom(job: JobRecord) = {
    createHal(job).toString(HAL_JSON)
  }

  def halFrom(jobs: Iterable[JobRecord]) = {
    val root = representationFactory.newRepresentation(selfLink)
    jobs.foreach(job => {
      root.withRepresentation("jobs", createHal(job))
    })
    root.toString(HAL_JSON)
  }

  def createHal(job: JobRecord) = {
    representationFactory.newRepresentation(s"${selfLink.toString}/${job.id.value}")
      .withProperty("jobId", job.id.value)
      .withLink("job", s"${jobLink.toString}/${job.id.value}")
      .withLink("complete", s"${completedJobLink.toString}")
  }
}
