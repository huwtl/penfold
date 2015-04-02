package org.huwtl.penfold.app.support.hal

import java.net.URI

import com.theoryinpractise.halbuilder.api.Representation
import com.theoryinpractise.halbuilder.api.RepresentationFactory._
import com.theoryinpractise.halbuilder.json.JsonRepresentationFactory
import org.huwtl.penfold.app.support.JavaMapUtil
import org.huwtl.penfold.domain.model.Status._
import org.huwtl.penfold.readstore.{PageRequest, TaskProjection, _}
import org.joda.time.format.DateTimeFormat

class HalTaskFormatter(baseTaskLink: URI, baseQueueLink: URI) extends PaginatedRepresentationProvider {
  private val representationFactory = new JsonRepresentationFactory().withFlag(COALESCE_ARRAYS)

  private val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  def halRepresentationFrom(task: TaskProjection) = {
    val representation = representationFactory.newRepresentation(s"${baseTaskLink.toString}/${task.id.value}")
      .withProperty("id", task.id.value)
      .withProperty("version", task.version.number)
      .withProperty("status", task.status.name)
      .withProperty("statusLastModified", dateFormatter.print(task.statusLastModified))
      .withProperty("created", dateFormatter.print(task.created))
      .withProperty("attempts", task.attempts)
      .withProperty("triggerDate", dateFormatter.print(task.triggerDate))
      .withProperty("score", task.score)
      .withProperty("payload", JavaMapUtil.deepConvertToJavaMap(task.payload.content))
      .withProperty("queue", task.queue.value)

    if (task.assignee.isDefined) {
      representation.withProperty("assignee", task.assignee.get.username)
    }

    if (task.rescheduleReason.isDefined) {
      representation.withProperty("rescheduleReason", task.rescheduleReason.get)
    }

    if (task.closeReason.isDefined) {
      representation.withProperty("closeReason", task.closeReason.get)
    }

    if (task.closeResultType.isDefined) {
      representation.withProperty("closeResultType", task.closeResultType.get.name)
    }

    if (task.cancelReason.isDefined) {
      representation.withProperty("cancelReason", task.cancelReason.get)
    }

    if (task.previousStatus.isDefined) {
      representation.withProperty("previousStatus", JavaMapUtil.deepConvertToJavaMap(previousStatusToMap(task.previousStatus.get)))
    }

    addLinks(task, representation)

    representation
  }

  def addLinks(task : TaskProjection, representation: Representation) = {
    val queueIdParam = task.queue.value

    val taskUpdateUrl = s"${baseTaskLink.toString}/${task.id.value}/${task.version.number}"

    representation.withLink("queue", s"${baseQueueLink.toString}/$queueIdParam/${task.status.name}")

    if (task.status != Cancelled) {
      if (task.status != Closed) {
        representation.withLink("UpdateTaskPayload", taskUpdateUrl)
        representation.withLink("CloseTask", taskUpdateUrl)
        representation.withLink("CancelTask", taskUpdateUrl)
      }

      representation.withLink("RescheduleTask", taskUpdateUrl)

      if (task.status != Ready) {
        representation.withLink("RequeueTask", taskUpdateUrl)
      }

      if (task.assignee.isDefined && (task.status == Waiting || task.status == Ready)) {
        representation.withLink("UnassignTask", taskUpdateUrl)
      }

      if (task.status == Ready) {
        representation.withLink("StartTask", taskUpdateUrl)
      }
    }
  }

  def halFrom(task: TaskProjection) = {
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

  def previousStatusToMap(previousStatus: PreviousStatus) = {
    Map("status" -> previousStatus.status.name, "statusLastModified" -> dateFormatter.print(previousStatus.statusLastModified))
  }
}
