package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{AggregateVersion, AggregateId}
import org.huwtl.penfold.command.{UnassignTask, UpdateTaskPayload}
import org.huwtl.penfold.domain.model.patch.Patch

case class UnassignTaskRequest(unassignType: Option[String], payloadUpdate: Option[Patch]) {
  def toCommand(id: AggregateId, version: AggregateVersion) = UnassignTask(id, version, unassignType, payloadUpdate)
}
