package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{Binding, Payload}
import org.joda.time.DateTime
import org.huwtl.penfold.command.{CreateTask, CreateFutureTask}

case class TaskCreationRequest(triggerDate: Option[DateTime],
                              payload: Payload,
                              binding: Binding) {

  def toCommand = {
    triggerDate match {
      case Some(date) => CreateFutureTask(binding, date, payload)
      case None => CreateTask(binding, payload)
    }
  }
}
