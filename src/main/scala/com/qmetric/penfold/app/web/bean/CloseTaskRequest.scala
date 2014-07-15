package com.qmetric.penfold.app.web.bean

import com.qmetric.penfold.domain.model.{Assignee, AggregateId}
import com.qmetric.penfold.command.CloseTask

case class CloseTaskRequest(id: AggregateId, concluder: Option[Assignee] = None, conclusionType: Option[String] = None) {
  def toCommand = CloseTask(id, concluder, conclusionType)
}
