package org.huwtl.penfold.app.support.hal

import com.theoryinpractise.halbuilder.api.RepresentationFactory._
import java.net.URI
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory
import org.huwtl.penfold.query.{Filters, PageResult, JobRecord}
import org.huwtl.penfold.domain.model.{QueueName, Status}

class HalQueueFormatter(baseQueueLink: URI, halJobFormatter: HalJobFormatter) extends PaginatedRepresentationProvider {
  private val representationFactory = new DefaultRepresentationFactory

  def halFrom(job: JobRecord) = {
    createHalQueueEntry(job).toString(HAL_JSON)
  }

  def halFrom(queueName: QueueName, status: Status, pageOfJobs: PageResult, filters: Filters = Filters.empty) = {
    val baseSelfLink = s"${baseQueueLink.toString}/${queueName.value}/${status.name}"

    val root = getRepresentation(pageOfJobs, filters, baseSelfLink, representationFactory)

    pageOfJobs.jobs.foreach(job => {
      root.withRepresentation("queue", createHalQueueEntry(job))
    })

    root.toString(HAL_JSON)
  }

  private def createHalQueueEntry(job: JobRecord) = {
    representationFactory.newRepresentation(s"${baseQueueLink.toString}/${job.queueName.value}/${job.status.name}/${job.id.value}")
      .withProperty("jobId", job.id.value)
      .withRepresentation("job", halJobFormatter.halRepresentationFrom(job))
  }
}
