package org.huwtl.penfold.app.support.hal

import com.theoryinpractise.halbuilder.api.RepresentationFactory._
import org.joda.time.format.DateTimeFormat
import java.net.URI
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory
import org.huwtl.penfold.readstore.{PageRequest, Filters, PageResult, JobRecord}
import org.huwtl.penfold.domain.model.Status._
import com.theoryinpractise.halbuilder.api.Representation
import org.huwtl.penfold.app.support.JavaMapUtil
import org.huwtl.penfold.domain.model.{Binding, QueueId}

class HalJobFormatter(baseJobLink: URI, baseQueueLink: URI) extends PaginatedRepresentationProvider {
  private val representationFactory = new DefaultRepresentationFactory

  private val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  def halRepresentationFrom(job: JobRecord, knownQueue: Option[QueueId] = None) = {
    val queueIdParam = knownQueue match {
      case Some(queueId) => queueId.value
      case None => if (job.binding.queues.size == 1) job.binding.queues.head.id.value else "{queueId}"
    }

    val representation: Representation = representationFactory.newRepresentation(s"${baseJobLink.toString}/${job.id.value}")
      .withProperty("id", job.id.value)
      .withProperty("status", job.status.name)
      .withProperty("triggerDate", dateFormatter.print(job.triggerDate))
      .withProperty("payload", JavaMapUtil.deepConvertToJavaMap(job.payload.content))
      .withProperty("binding", JavaMapUtil.deepConvertToJavaMap(bindingToMap(job.binding)))
      .withLink("queue", s"${baseQueueLink.toString}/$queueIdParam")

    job.status match {
      case Ready => {
        representation.withLink("start", s"${baseQueueLink.toString}/$queueIdParam/${Started.name}")
      }
      case Started => {
        representation.withLink("complete", s"${baseQueueLink.toString}/$queueIdParam/${Completed.name}")
      }
      case _ =>
    }

    representation
  }

  def halFrom(job: JobRecord) = {
    halRepresentationFrom(job).toString(HAL_JSON)
  }

  def halFrom(pageRequest: PageRequest, pageOfJobs: PageResult, filters: Filters = Filters.empty) = {
    val baseSelfLink = s"${baseJobLink.toString}"

    val root = getRepresentation(pageRequest, pageOfJobs, filters, baseSelfLink, representationFactory)

    pageOfJobs.entries.foreach(job => {
      root.withRepresentation("jobs", halRepresentationFrom(job))
    })

    root.toString(HAL_JSON)
  }

  def bindingToMap(binding: Binding) = {
    Map("queues" -> binding.queues.map(queue => Map("id" -> queue.id.value)))
  }
}
