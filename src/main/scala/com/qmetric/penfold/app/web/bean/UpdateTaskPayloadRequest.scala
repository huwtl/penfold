package com.qmetric.penfold.app.web.bean

import com.qmetric.penfold.domain.model.{Payload, AggregateVersion, AggregateId}
import com.qmetric.penfold.command.UpdateTaskPayload
import com.qmetric.penfold.domain.model.patch.Patch

case class UpdateTaskPayloadRequest(updateType: Option[String], payloadUpdate: Patch, scoreUpdate: Option[Long]) {
  def toCommand(id: AggregateId, version: AggregateVersion) = UpdateTaskPayload(id, version, updateType, payloadUpdate, scoreUpdate)
}
