package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{Payload, AggregateVersion, AggregateId}
import org.huwtl.penfold.command.UpdateTaskPayload
import org.huwtl.penfold.domain.model.patch.Patch

case class UpdateTaskPayloadRequest(updateType: Option[String], payloadUpdate: Patch, score: Option[Long]) {
  def toCommand(id: AggregateId, version: AggregateVersion) = UpdateTaskPayload(id, version, updateType, payloadUpdate, score)
}
