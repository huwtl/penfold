package com.qmetric.penfold.app.readstore.postgres.subscribers

import com.qmetric.penfold.app.readstore.postgres.TaskData
import com.qmetric.penfold.domain.event.{Event, TaskRequeued}
import com.qmetric.penfold.domain.model.Status.Ready

class TaskRequeuedSubscriber extends TaskUpdateSubscriber[TaskRequeued] {

  override def applicable(event: Event) = event.isInstanceOf[TaskRequeued]

  override def handleUpdateEvent(event: TaskRequeued, existingTask: TaskData) = {
    val score = event.score.getOrElse(existingTask.score)

    existingTask.copy(
      previousStatus = Some(updatePreviousStatus(existingTask)),
      status = Ready,
      statusLastModified = event.created.getMillis,
      score = score,
      sort = score,
      assignee = event.assignee,
      payload = patchPayloadIfExists(existingTask, event.payloadUpdate)
    )
  }
}
