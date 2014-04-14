package org.huwtl.penfold.app.support.hal

import com.theoryinpractise.halbuilder.api.RepresentationFactory._
import org.joda.time.format.DateTimeFormat
import java.net.URI
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory
import org.huwtl.penfold.readstore.{PageRequest, Filters, PageResult, TaskRecord}
import org.huwtl.penfold.domain.model.Status._
import com.theoryinpractise.halbuilder.api.Representation
import org.huwtl.penfold.app.support.JavaMapUtil
import org.huwtl.penfold.domain.model.{Binding, QueueId}

class HalTaskFormatter(baseTaskLink: URI, baseQueueLink: URI) extends PaginatedRepresentationProvider {
  private val representationFactory = new DefaultRepresentationFactory

  private val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  def halRepresentationFrom(task: TaskRecord, knownQueue: Option[QueueId] = None) = {
    val queueIdParam = knownQueue match {
      case Some(queueId) => queueId.value
      case None => if (task.binding.queues.size == 1) task.binding.queues.head.id.value else "{queueId}"
    }

    val representation: Representation = representationFactory.newRepresentation(s"${baseTaskLink.toString}/${task.id.value}")
      .withProperty("id", task.id.value)
      .withProperty("status", task.status.name)
      .withProperty("triggerDate", dateFormatter.print(task.triggerDate))
      .withProperty("payload", JavaMapUtil.deepConvertToJavaMap(task.payload.content))
      .withProperty("binding", JavaMapUtil.deepConvertToJavaMap(bindingToMap(task.binding)))
      .withLink("queue", s"${baseQueueLink.toString}/$queueIdParam")

    task.status match {
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

  def halFrom(task: TaskRecord) = {
    halRepresentationFrom(task).toString(HAL_JSON)
  }

  def halFrom(pageRequest: PageRequest, pageOfTasks: PageResult, filters: Filters = Filters.empty) = {
    val baseSelfLink = s"${baseTaskLink.toString}"

    val root = getRepresentation(pageRequest, pageOfTasks, filters, baseSelfLink, representationFactory)

    pageOfTasks.entries.foreach(task => {
      root.withRepresentation("tasks", halRepresentationFrom(task))
    })

    root.toString(HAL_JSON)
  }

  def bindingToMap(binding: Binding) = {
    Map("queues" -> binding.queues.map(queue => Map("id" -> queue.id.value)))
  }
}
