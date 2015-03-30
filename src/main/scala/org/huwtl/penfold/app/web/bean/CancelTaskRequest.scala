package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.command.CancelTask
import org.huwtl.penfold.domain.model.patch.Patch
import org.huwtl.penfold.domain.model.{AggregateId, AggregateVersion, User}

case class CancelTaskRequest(user: Option[User] = None, reason: Option[String] = None, payloadUpdate: Option[Patch]) {
  def toCommand(id: AggregateId, version: AggregateVersion) = CancelTask(id, version, user, reason, payloadUpdate)
}
