package org.huwtl.penfold.app.support.hal

import com.theoryinpractise.halbuilder.api.RepresentationFactory._
import java.net.URI
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory
import org.huwtl.penfold.readstore.{PageRequest, Filters, PageResult, JobRecord}
import org.huwtl.penfold.domain.model.{QueueId, Status}

class HalQueueFormatter(baseQueueLink: URI, halJobFormatter: HalJobFormatter) extends PaginatedRepresentationProvider {
  private val representationFactory = new DefaultRepresentationFactory

  def halFrom(queueId: QueueId, job: JobRecord) = {
    createHalQueueEntry(queueId, job).toString(HAL_JSON)
  }

  def halFrom(queueId: QueueId, status: Status, pageRequest: PageRequest, pageOfJobs: PageResult, filters: Filters = Filters.empty) = {
    val baseSelfLink = s"${baseQueueLink.toString}/${queueId.value}/${status.name}"

    val root = getRepresentation(pageRequest, pageOfJobs, filters, baseSelfLink, representationFactory)

    pageOfJobs.entries.foreach(job => {
      root.withRepresentation("queue", createHalQueueEntry(queueId, job))
    })

    root.toString(HAL_JSON)
  }

  private def createHalQueueEntry(queueId: QueueId, job: JobRecord) = {
    representationFactory.newRepresentation(s"${baseQueueLink.toString}/${queueId.value}/${job.status.name}/${job.id.value}")
      .withProperty("jobId", job.id.value)
      .withRepresentation("job", halJobFormatter.halRepresentationFrom(job, Some(queueId)))
  }
}
