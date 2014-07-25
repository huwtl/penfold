package com.qmetric.penfold.app.web.bean

import com.qmetric.penfold.domain.model.{AggregateVersion, User, AggregateId}
import com.qmetric.penfold.command.CloseTask
import com.qmetric.penfold.domain.model.patch.Patch

case class CloseTaskRequest(concluder: Option[User] = None, conclusionType: Option[String] = None, assignee: Option[User], payloadUpdate: Option[Patch]) {
  def toCommand(id: AggregateId, version: AggregateVersion) = CloseTask(id, version, concluder, conclusionType, assignee, payloadUpdate)
}
