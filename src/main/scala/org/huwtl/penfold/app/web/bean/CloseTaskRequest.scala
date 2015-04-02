package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{CloseResultType, AggregateVersion, User, AggregateId}
import org.huwtl.penfold.command.CloseTask
import org.huwtl.penfold.domain.model.patch.Patch

case class CloseTaskRequest(user: Option[User] = None, reason: Option[String] = None, resultType: Option[CloseResultType] = None, payloadUpdate: Option[Patch]) {
  def toCommand(id: AggregateId, version: AggregateVersion) = CloseTask(id, version, user, reason, resultType, payloadUpdate)
}
