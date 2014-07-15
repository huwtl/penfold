package com.qmetric.penfold.app.web.bean

import com.qmetric.penfold.domain.model.{Assignee, AggregateId}
import com.qmetric.penfold.command.{RescheduleTask, RequeueTask}
import org.joda.time.DateTime

case class RescheduleTaskRequest(id: AggregateId, triggerDate: DateTime, assignee: Option[Assignee], rescheduleType: Option[String]) {
  def toCommand = RescheduleTask(id, triggerDate, assignee, rescheduleType)
}
