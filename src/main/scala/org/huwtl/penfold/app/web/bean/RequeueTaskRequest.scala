package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{User, AggregateVersion, AggregateId}
import org.huwtl.penfold.command.RequeueTask
import org.huwtl.penfold.domain.model.patch.Patch

case class RequeueTaskRequest(assignee: Option[User], reason: Option[String], payloadUpdate: Option[Patch], scoreUpdate: Option[Long]) {
  def toCommand(id: AggregateId, version: AggregateVersion) = RequeueTask(id, version, reason, assignee, payloadUpdate, scoreUpdate)
}
