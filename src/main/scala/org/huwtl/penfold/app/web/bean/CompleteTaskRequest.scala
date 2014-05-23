package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{User, AggregateId}
import org.huwtl.penfold.command.CompleteTask

case class CompleteTaskRequest(id: AggregateId, concluder: Option[User] = None, conclusionType: Option[String] = None) {
  def toCommand = CompleteTask(id, concluder, conclusionType)
}
