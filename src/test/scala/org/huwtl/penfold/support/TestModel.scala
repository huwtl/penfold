package org.huwtl.penfold.support

import org.joda.time.DateTime
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.readstore.{PreviousStatus, TaskRecord}
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.model.Status.{Waiting, Started, Ready, Closed}

object TestModel
{
  val createdDate = new DateTime(2014, 2, 25, 13, 0, 0, 0)

  val triggerDate = new DateTime(2014, 2, 25, 14, 0, 0, 0)

  val score = triggerDate.getMillis

  val aggregateId = AggregateId("1")

  val queueId = QueueId("abc")

  val assignee = Assignee("user1")

  val concluder = User("user1")

  val conclusionType = "type"

  val emptyPayload = Payload.empty

  val payload = Payload(Map("data" -> "value", "inner" -> Map("bool" -> true)))

  val complexPayload = Payload(Map("data" -> "value", "inner" -> Map("bool" -> true, "inner2" -> List(Map("a" -> "1", "b" -> 1), Map("a" -> "2", "b" -> 2)))))

  val previousStatus = PreviousStatus(Started, new DateTime(2014, 2, 25, 13, 0, 0, 0))

  val task = TaskRecord(aggregateId, AggregateVersion.init, createdDate, QueueBinding(queueId), Status.Ready, createdDate, None, None, triggerDate, score, score, payload)

  val readyTask = task

  val waitingTask = task.copy(status = Waiting)

  val startedTask = task.copy(version = AggregateVersion(2), status = Started, assignee = Some(assignee), previousStatus = Some(PreviousStatus(Ready, createdDate)), sort = createdDate.getMillis)

  val closedTask = startedTask.copy(version = AggregateVersion(3), status = Closed, previousStatus = Some(previousStatus), concluder = Some(concluder), conclusionType = Some(conclusionType))

  val requeuedTask = task.copy(version = AggregateVersion(2), previousStatus = Some(previousStatus))
}
