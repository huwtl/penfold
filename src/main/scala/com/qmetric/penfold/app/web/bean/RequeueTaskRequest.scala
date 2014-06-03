package com.qmetric.penfold.app.web.bean

import com.qmetric.penfold.domain.model.AggregateId
import com.qmetric.penfold.command.RequeueTask

case class RequeueTaskRequest(id: AggregateId) {
  def toCommand = RequeueTask(id)
}
