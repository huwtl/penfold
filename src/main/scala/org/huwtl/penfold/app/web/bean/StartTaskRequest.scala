package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{QueueId, AggregateId}
import org.huwtl.penfold.command.StartTask

case class StartTaskRequest(id: AggregateId) {
  def toCommand(queue: QueueId) = StartTask(id, queue)
}
