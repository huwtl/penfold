package com.qmetric.penfold.app.web.bean

import com.qmetric.penfold.domain.model.{AggregateVersion, AggregateId}
import com.qmetric.penfold.command.{UnassignTask, UpdateTaskPayload}
import com.qmetric.penfold.domain.model.patch.Patch

case class UnassignTaskRequest(unassignType: Option[String], payloadUpdate: Option[Patch]) {
  def toCommand(id: AggregateId, version: AggregateVersion) = UnassignTask(id, version, unassignType, payloadUpdate)
}
