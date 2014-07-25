package com.qmetric.penfold.app.web.bean

import com.qmetric.penfold.domain.model.{User, AggregateVersion, AggregateId}
import com.qmetric.penfold.command.RequeueTask
import com.qmetric.penfold.domain.model.patch.Patch

case class RequeueTaskRequest(assignee: Option[User], requeueType: Option[String], payloadUpdate: Option[Patch], scoreUpdate: Option[Long]) {
  def toCommand(id: AggregateId, version: AggregateVersion) = RequeueTask(id, version, requeueType, assignee, payloadUpdate, scoreUpdate)
}
