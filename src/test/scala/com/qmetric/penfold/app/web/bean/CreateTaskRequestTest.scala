package com.qmetric.penfold.app.web.bean

import org.specs2.mutable.Specification
import com.qmetric.penfold.domain.model.{QueueId, QueueBinding, Payload}
import com.qmetric.penfold.command.{CreateFutureTask, CreateTask}
import org.joda.time.DateTime

class CreateTaskRequestTest extends Specification {
  "convert to task creation command" in {
    val request = CreateTaskRequest(None, Payload.empty, QueueBinding(QueueId("q1")), None)
    request.toCommand must beEqualTo(CreateTask(QueueBinding(QueueId("q1")), Payload.empty, None))
  }

  "convert to future task creation command" in {
    val triggerDate = DateTime.now
    val request = CreateTaskRequest(Some(triggerDate), Payload.empty, QueueBinding(QueueId("q1")), None)
    request.toCommand must beEqualTo(CreateFutureTask(QueueBinding(QueueId("q1")), triggerDate, Payload.empty, None))
  }
}
