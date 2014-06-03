package com.qmetric.penfold.app.web.bean

import com.qmetric.penfold.domain.model.{QueueBinding, Payload}
import org.joda.time.DateTime
import com.qmetric.penfold.command.{CreateTask, CreateFutureTask}

case class CreateTaskRequest(triggerDate: Option[DateTime],
                             payload: Payload,
                             queueBinding: QueueBinding,
                             score: Option[Long]) {

  def toCommand = {
    triggerDate match {
      case Some(date) => CreateFutureTask(queueBinding, date, payload, score)
      case None => CreateTask(queueBinding, payload, score)
    }
  }
}
