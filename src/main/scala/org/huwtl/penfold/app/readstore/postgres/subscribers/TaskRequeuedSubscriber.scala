package org.huwtl.penfold.app.readstore.postgres.subscribers

import org.huwtl.penfold.app.readstore.postgres.TaskData
import org.huwtl.penfold.domain.event.{Event, TaskRequeued}
import org.huwtl.penfold.domain.model.Status.Ready

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
