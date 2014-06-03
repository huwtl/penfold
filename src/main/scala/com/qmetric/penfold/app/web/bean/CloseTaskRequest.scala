package com.qmetric.penfold.app.web.bean

import com.qmetric.penfold.domain.model.{User, AggregateId}
import com.qmetric.penfold.command.CloseTask

case class CloseTaskRequest(id: AggregateId, concluder: Option[User] = None, conclusionType: Option[String] = None) {
  def toCommand = CloseTask(id, concluder, conclusionType)
}
