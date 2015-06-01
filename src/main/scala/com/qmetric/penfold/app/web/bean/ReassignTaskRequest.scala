package com.qmetric.penfold.app.web.bean

import com.qmetric.penfold.command.ReassignTask
import com.qmetric.penfold.domain.model.{AggregateId, AggregateVersion, User}
import com.qmetric.penfold.domain.model.patch.Patch

case class ReassignTaskRequest(assignee: User, reassignType: Option[String], payloadUpdate: Option[Patch]) {
  def toCommand(id: AggregateId, version: AggregateVersion) = ReassignTask(id, version, assignee, reassignType, payloadUpdate)
}
