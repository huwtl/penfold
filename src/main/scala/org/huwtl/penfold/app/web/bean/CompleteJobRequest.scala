package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{QueueId, AggregateId}
import org.huwtl.penfold.command.{CompleteJob, StartJob}

case class CompleteJobRequest(id: AggregateId) {
  def toCommand(queue: QueueId) = CompleteJob(id, queue)
}
