package com.qmetric.penfold.app.web.bean

import com.qmetric.penfold.domain.model.{CloseResultType, AggregateVersion, User, AggregateId}
import com.qmetric.penfold.command.CloseTask
import com.qmetric.penfold.domain.model.patch.Patch

case class CloseTaskRequest(user: Option[User] = None, reason: Option[String] = None, resultType: Option[CloseResultType] = None, payloadUpdate: Option[Patch]) {
  def toCommand(id: AggregateId, version: AggregateVersion) = CloseTask(id, version, user, reason, resultType, payloadUpdate)
}
