package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{QueueBinding, Payload}
import org.joda.time.DateTime
import org.huwtl.penfold.command.{CreateTask, CreateFutureTask}

case class TaskCreationRequest(triggerDate: Option[DateTime],
                              payload: Payload,
                              queueBinding: QueueBinding) {

  def toCommand = {
    triggerDate match {
      case Some(date) => CreateFutureTask(queueBinding, date, payload)
      case None => CreateTask(queueBinding, payload)
    }
  }
}
