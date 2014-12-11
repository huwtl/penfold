package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{AggregateVersion, User, AggregateId}
import org.huwtl.penfold.command.CloseTask
import org.huwtl.penfold.domain.model.patch.Patch

case class CloseTaskRequest(concluder: Option[User] = None, conclusionType: Option[String] = None, assignee: Option[User], payloadUpdate: Option[Patch]) {
  def toCommand(id: AggregateId, version: AggregateVersion) = CloseTask(id, version, concluder, conclusionType, assignee, payloadUpdate)
}
