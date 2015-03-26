package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.command.CreateFutureTask
import org.huwtl.penfold.domain.model.{QueueId, Payload}
import org.joda.time.DateTime

case class CreateFutureTaskRequest(triggerDate: DateTime,
                                   payload: Payload,
                                   queue: QueueId,
                                   score: Option[Long]) {

  def toCommand = CreateFutureTask(queue, triggerDate, payload, score)
}
