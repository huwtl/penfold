package org.huwtl.penfold.app.support.hal

import com.theoryinpractise.halbuilder.api.RepresentationFactory._
import java.net.URI
import com.theoryinpractise.halbuilder.json.JsonRepresentationFactory
import org.huwtl.penfold.readstore.{PageRequest, Filters, PageResult, TaskProjection}
import org.huwtl.penfold.domain.model.{QueueId, Status}

class HalQueueFormatter(baseQueueLink: URI, halTaskFormatter: HalTaskFormatter) extends PaginatedRepresentationProvider {
  private val representationFactory = new JsonRepresentationFactory().withFlag(COALESCE_ARRAYS)

  def halFrom(queueId: QueueId, task: TaskProjection) = {
    createHalQueueEntry(queueId, task).toString(HAL_JSON)
  }

  def halFrom(queueId: QueueId, status: Status, pageRequest: PageRequest, pageOfTasks: PageResult, filters: Filters = Filters.empty) = {
    val baseSelfLink = s"${baseQueueLink.toString}/${queueId.value}/${status.name}"

    val root = getRepresentation(pageRequest, pageOfTasks, filters, baseSelfLink, representationFactory)

    pageOfTasks.entries.foreach(task => {
      root.withRepresentation("queue", createHalQueueEntry(queueId, task))
    })

    root.toString(HAL_JSON)
  }

  private def createHalQueueEntry(queueId: QueueId, task: TaskProjection) = {
    representationFactory.newRepresentation(s"${baseQueueLink.toString}/${queueId.value}/${task.status.name}/${task.id.value}")
      .withProperty("taskId", task.id.value)
      .withRepresentation("task", halTaskFormatter.halRepresentationFrom(task))
  }
}
