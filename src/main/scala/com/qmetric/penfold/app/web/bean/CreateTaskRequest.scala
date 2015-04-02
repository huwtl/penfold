package com.qmetric.penfold.app.web.bean

import com.qmetric.penfold.command.CreateTask
import com.qmetric.penfold.domain.model.{QueueId, Payload}

case class CreateTaskRequest(payload: Payload,
                             queue: QueueId,
                             score: Option[Long]) {

  def toCommand = CreateTask(queue, payload, score)
}
