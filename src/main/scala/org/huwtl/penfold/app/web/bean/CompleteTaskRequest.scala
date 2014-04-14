package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{QueueId, AggregateId}
import org.huwtl.penfold.command.CompleteTask

case class CompleteTaskRequest(id: AggregateId) {
  def toCommand(queue: QueueId) = CompleteTask(id, queue)
}
