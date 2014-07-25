package com.qmetric.penfold.app.web.bean

import com.qmetric.penfold.domain.model.{QueueBinding, Payload}
import com.qmetric.penfold.command.CreateTask

case class CreateTaskRequest(payload: Payload,
                             queueBinding: QueueBinding,
                             score: Option[Long]) {

  def toCommand = CreateTask(queueBinding, payload, score)
}
