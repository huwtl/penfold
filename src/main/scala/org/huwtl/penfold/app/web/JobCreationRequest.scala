package org.huwtl.penfold.app.web

import org.huwtl.penfold.domain.model.{Binding, Payload, AggregateId}
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
