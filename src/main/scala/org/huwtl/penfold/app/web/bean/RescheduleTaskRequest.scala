package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{AggregateVersion, User, AggregateId}
import org.huwtl.penfold.command.RescheduleTask
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.patch.Patch

case class RescheduleTaskRequest(triggerDate: DateTime, assignee: Option[User], rescheduleType: Option[String], payloadUpdate: Option[Patch], scoreUpdate: Option[Long]) {
  def toCommand(id: AggregateId, version: AggregateVersion) = RescheduleTask(id, version, triggerDate, assignee, rescheduleType, payloadUpdate, scoreUpdate)
}
