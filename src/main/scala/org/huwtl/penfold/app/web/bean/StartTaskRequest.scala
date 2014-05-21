package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.{Assignee, AggregateId}
import org.huwtl.penfold.command.StartTask

case class StartTaskRequest(id: AggregateId, assignee: Option[Assignee]) {
  def toCommand = StartTask(id, assignee)
}
