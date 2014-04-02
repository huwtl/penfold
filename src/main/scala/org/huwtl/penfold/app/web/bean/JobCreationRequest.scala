package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{Binding, Payload}
import org.joda.time.DateTime
import org.huwtl.penfold.command.{CreateJob, CreateFutureJob}

case class JobCreationRequest(triggerDate: Option[DateTime],
                              payload: Payload,
                              binding: Binding) {

  def toCommand = {
    triggerDate match {
      case Some(date) => CreateFutureJob(binding, date, payload)
      case None => CreateJob(binding, payload)
    }
  }
}
