package com.qmetric.penfold.app.web.bean

import com.qmetric.penfold.domain.model.{Assignee, AggregateId}
import com.qmetric.penfold.command.StartTask

case class StartTaskRequest(id: AggregateId, assignee: Option[Assignee]) {
  def toCommand = StartTask(id, assignee)
}
