package com.qmetric.penfold.app.web.bean

import com.qmetric.penfold.domain.model.{AggregateVersion, User, AggregateId}
import com.qmetric.penfold.command.StartTask
import com.qmetric.penfold.domain.model.patch.Patch

case class StartTaskRequest(assignee: Option[User], payloadUpdate: Option[Patch]) {
  def toCommand(id: AggregateId, version: AggregateVersion) = StartTask(id, version, assignee, payloadUpdate)
}
