package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{QueueBinding, Payload}
import org.huwtl.penfold.command.CreateTask

case class CreateTaskRequest(payload: Payload,
                             queueBinding: QueueBinding,
                             score: Option[Long]) {

  def toCommand = CreateTask(queueBinding, payload, score)
}
