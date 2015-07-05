package org.huwtl.penfold.app.readstore.postgres.subscribers

import org.huwtl.penfold.app.readstore.postgres.TaskData
import org.huwtl.penfold.domain.event.{Event, TaskStarted}
import org.huwtl.penfold.domain.model.Status.Started

class TaskStartedSubscriber extends TaskUpdateSubscriber[TaskStarted] {

  override def applicable(event: Event) = event.isInstanceOf[TaskStarted]

  override def handleUpdateEvent(event: TaskStarted, existingTask: TaskData) = {
    existingTask.copy(
      previousStatus = Some(updatePreviousStatus(existingTask)),
      status = Started,
      statusLastModified = event.created.getMillis,
      attempts = existingTask.attempts + 1,
      sort = event.created.getMillis,
      assignee = event.assignee,
      payload = patchPayloadIfExists(existingTask, event.payloadUpdate)
    )
  }
}
