package com.qmetric.penfold.app.web.bean

import com.qmetric.penfold.domain.model.{AggregateVersion, User, AggregateId}
import com.qmetric.penfold.command.RescheduleTask
import org.joda.time.DateTime
import com.qmetric.penfold.domain.model.patch.Patch

case class RescheduleTaskRequest(triggerDate: DateTime, assignee: Option[User], reason: Option[String], payloadUpdate: Option[Patch], scoreUpdate: Option[Long]) {
  def toCommand(id: AggregateId, version: AggregateVersion) = RescheduleTask(id, version, triggerDate, assignee, reason, payloadUpdate, scoreUpdate)
}
