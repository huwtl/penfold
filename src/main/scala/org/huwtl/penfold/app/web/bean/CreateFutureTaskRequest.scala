package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{QueueBinding, Payload}
import org.joda.time.DateTime
import org.huwtl.penfold.command.CreateFutureTask

case class CreateFutureTaskRequest(triggerDate: DateTime,
                                   payload: Payload,
                                   queueBinding: QueueBinding,
                                   score: Option[Long]) {

  def toCommand = CreateFutureTask(queueBinding, triggerDate, payload, score)
}
