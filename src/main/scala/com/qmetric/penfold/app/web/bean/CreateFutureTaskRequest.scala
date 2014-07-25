package com.qmetric.penfold.app.web.bean

import com.qmetric.penfold.domain.model.{QueueBinding, Payload}
import org.joda.time.DateTime
import com.qmetric.penfold.command.CreateFutureTask

case class CreateFutureTaskRequest(triggerDate: DateTime,
                                   payload: Payload,
                                   queueBinding: QueueBinding,
                                   score: Option[Long]) {

  def toCommand = CreateFutureTask(queueBinding, triggerDate, payload, score)
}
