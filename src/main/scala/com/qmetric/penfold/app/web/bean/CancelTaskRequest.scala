package com.qmetric.penfold.app.web.bean

import com.qmetric.penfold.command.CancelTask
import com.qmetric.penfold.domain.model.patch.Patch
import com.qmetric.penfold.domain.model.{AggregateId, AggregateVersion, User}

case class CancelTaskRequest(user: Option[User] = None, reason: Option[String] = None, payloadUpdate: Option[Patch]) {
  def toCommand(id: AggregateId, version: AggregateVersion) = CancelTask(id, version, user, reason, payloadUpdate)
}
