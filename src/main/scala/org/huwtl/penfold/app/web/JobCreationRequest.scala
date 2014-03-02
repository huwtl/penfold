package org.huwtl.penfold.app.web

import org.huwtl.penfold.domain.model.{Payload, QueueName, AggregateId}
import org.joda.time.DateTime
import org.huwtl.penfold.command.{CreateJob, CreateFutureJob, Command}

case class JobCreationRequest(id: AggregateId,
                              queueName: QueueName,
                              triggerDate: Option[DateTime],
                              payload: Payload) {

  def toCommand = {
    triggerDate match {
      case Some(date) => CreateFutureJob(id, queueName, date, payload)
      case None => CreateJob(id, queueName, payload)
    }
  }
}
