package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.command.CreateTask
import org.huwtl.penfold.domain.model.{QueueId, Payload}

case class CreateTaskRequest(payload: Payload,
                             queue: QueueId,
                             score: Option[Long]) {

  def toCommand = CreateTask(queue, payload, score)
}
