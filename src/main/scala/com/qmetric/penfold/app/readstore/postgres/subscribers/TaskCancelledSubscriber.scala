package com.qmetric.penfold.app.readstore.postgres.subscribers

import com.qmetric.penfold.app.readstore.postgres.TaskData
import com.qmetric.penfold.domain.event.{Event, TaskCancelled}
import com.qmetric.penfold.domain.model.Status.Cancelled

class TaskCancelledSubscriber extends TaskUpdateSubscriber[TaskCancelled] {

  override def applicable(event: Event) = event.isInstanceOf[TaskCancelled]

  override def handleUpdateEvent(event: TaskCancelled, existingTask: TaskData) = {
    existingTask.copy(
      previousStatus = Some(updatePreviousStatus(existingTask)),
      status = Cancelled,
      statusLastModified = event.created.getMillis,
      sort = event.created.getMillis,
      cancelReason = event.reason,
      assignee = None,
      payload = patchPayloadIfExists(existingTask, event.payloadUpdate)
    )
  }
}
