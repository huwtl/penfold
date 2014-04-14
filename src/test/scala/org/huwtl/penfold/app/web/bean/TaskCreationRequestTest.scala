package org.huwtl.penfold.app.web.bean

import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.model.{QueueId, QueueBinding, Payload}
import org.huwtl.penfold.command.{CreateFutureTask, CreateTask}
import org.joda.time.DateTime

class TaskCreationRequestTest extends Specification {
  "convert to task creation command" in {
    val request = TaskCreationRequest(None, Payload.empty, QueueBinding(QueueId("q1")))
    request.toCommand must beEqualTo(CreateTask(QueueBinding(QueueId("q1")), Payload.empty))
  }

  "convert to future task creation command" in {
    val triggerDate = DateTime.now
    val request = TaskCreationRequest(Some(triggerDate), Payload.empty, QueueBinding(QueueId("q1")))
    request.toCommand must beEqualTo(CreateFutureTask(QueueBinding(QueueId("q1")), triggerDate, Payload.empty))
  }
}
