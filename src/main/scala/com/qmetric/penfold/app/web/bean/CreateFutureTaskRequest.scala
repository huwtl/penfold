package com.qmetric.penfold.app.web.bean

import com.qmetric.penfold.command.CreateFutureTask
import com.qmetric.penfold.domain.model.{QueueId, Payload}
import org.joda.time.DateTime

case class CreateFutureTaskRequest(triggerDate: DateTime,
                                   payload: Payload,
                                   queue: QueueId,
                                   score: Option[Long]) {

  def toCommand = CreateFutureTask(queue, triggerDate, payload, score)
}
