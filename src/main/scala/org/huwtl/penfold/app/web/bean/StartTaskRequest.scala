package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{AggregateVersion, User, AggregateId}
import org.huwtl.penfold.command.StartTask
import org.huwtl.penfold.domain.model.patch.Patch

case class StartTaskRequest(assignee: Option[User], payloadUpdate: Option[Patch]) {
  def toCommand(id: AggregateId, version: AggregateVersion) = StartTask(id, version, assignee, payloadUpdate)
}
