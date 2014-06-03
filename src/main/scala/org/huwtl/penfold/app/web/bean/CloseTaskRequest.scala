package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{User, AggregateId}
import org.huwtl.penfold.command.CloseTask

case class CloseTaskRequest(id: AggregateId, concluder: Option[User] = None, conclusionType: Option[String] = None) {
  def toCommand = CloseTask(id, concluder, conclusionType)
}
