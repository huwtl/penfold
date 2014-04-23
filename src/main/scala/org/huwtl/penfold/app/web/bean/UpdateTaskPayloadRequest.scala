package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{Payload, AggregateVersion, AggregateId}
import org.huwtl.penfold.command.UpdateTaskPayload

case class UpdateTaskPayloadRequest(updateType: Option[String], payload: Payload, score: Option[Long]) {
  def toCommand(id: AggregateId, version: AggregateVersion) = UpdateTaskPayload(id, version, updateType, payload, score)
}
