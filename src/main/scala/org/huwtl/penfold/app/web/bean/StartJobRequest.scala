package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{QueueId, AggregateId}
import org.huwtl.penfold.command.StartJob

case class StartJobRequest(id: AggregateId) {
  def toCommand(queue: QueueId) = StartJob(id, queue)
}
