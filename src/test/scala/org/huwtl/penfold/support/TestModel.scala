package org.huwtl.penfold.support

import org.joda.time.DateTime
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.readstore.{PreviousStatus, TaskRecord}
import org.huwtl.penfold.domain.model.QueueId

object TestModel
{
  val createdDate = new DateTime(2014, 2, 25, 13, 0, 0, 0)

  val triggerDate = new DateTime(2014, 2, 25, 14, 0, 0, 0)

  val aggregateId = AggregateId("1")

  val queueId = QueueId("abc")

  val assignee = Assignee("user1")

  val emptyPayload = Payload.empty

  val payload = Payload(Map("data" -> "value", "inner" -> Map("bool" -> true)))

  val complexPayload = Payload(Map("data" -> "value", "inner" -> Map("bool" -> true, "inner2" -> List(Map("a" -> "1", "b" -> 1), Map("a" -> "2", "b" -> 2)))))

  val previousStatus = PreviousStatus(Status.Ready, new DateTime(2014, 2, 14, 12, 0, 0, 0))

  val task = TaskRecord(aggregateId, AggregateVersion.init, createdDate, QueueBinding(queueId), Status.Ready, createdDate, None, None, triggerDate, triggerDate.getMillis, triggerDate.getMillis, payload)
}
