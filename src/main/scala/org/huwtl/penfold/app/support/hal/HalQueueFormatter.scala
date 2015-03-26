package org.huwtl.penfold.app.support.hal

import java.net.URI

import com.theoryinpractise.halbuilder.api.RepresentationFactory._
import com.theoryinpractise.halbuilder.json.JsonRepresentationFactory
import org.huwtl.penfold.domain.model.{QueueId, Status}
import org.huwtl.penfold.readstore.{Filters, PageRequest, PageResult}

class HalQueueFormatter(baseQueueLink: URI, halTaskFormatter: HalTaskFormatter) extends PaginatedRepresentationProvider {
  private val representationFactory = new JsonRepresentationFactory().withFlag(COALESCE_ARRAYS)

  def halFrom(queueId: QueueId, status: Status, pageRequest: PageRequest, pageOfTasks: PageResult, filters: Filters = Filters.empty) = {
    val baseSelfLink = s"${baseQueueLink.toString}/${queueId.value}/${status.name}"

    val root = getRepresentation(pageRequest, pageOfTasks, filters, baseSelfLink, representationFactory)

    root.withProperty("id", queueId.value)

    pageOfTasks.entries.foreach(task => {
      root.withRepresentation("tasks", halTaskFormatter.halRepresentationFrom(task))
    })

    root.toString(HAL_JSON)
  }
}
