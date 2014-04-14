package org.huwtl.penfold.app.web.bean

import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.model.{Binding, Payload}
import org.huwtl.penfold.command.{CreateFutureTask, CreateTask}
import org.joda.time.DateTime

class TaskCreationRequestTest extends Specification {
  "convert to task creation command" in {
    val request = TaskCreationRequest(None, Payload.empty, Binding(List()))
    request.toCommand must beEqualTo(CreateTask(Binding(List()), Payload.empty))
  }

  "convert to future task creation command" in {
    val triggerDate = DateTime.now
    val request = TaskCreationRequest(Some(triggerDate), Payload.empty, Binding(List()))
    request.toCommand must beEqualTo(CreateFutureTask(Binding(List()), triggerDate, Payload.empty))
  }
}
