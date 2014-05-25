package org.huwtl.penfold.app.web.bean

import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.command.RequeueTask

case class RequeueTaskRequest(id: AggregateId) {
  def toCommand = RequeueTask(id)
}
