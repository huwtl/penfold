package com.qmetric.penfold.app.readstore.postgres.subscribers

import com.qmetric.penfold.app.readstore.postgres.TaskData
import com.qmetric.penfold.domain.event.{Event, TaskClosed}
import com.qmetric.penfold.domain.model.Status.Closed

class TaskClosedSubscriber extends TaskUpdateSubscriber[TaskClosed] {

  override def applicable(event: Event) = event.isInstanceOf[TaskClosed]

  override def handleUpdateEvent(event: TaskClosed, existingTask: TaskData) = {
    existingTask.copy(
      previousStatus = Some(updatePreviousStatus(existingTask)),
      status = Closed,
      statusLastModified = event.created.getMillis,
      sort = event.created.getMillis,
      closeReason = event.reason,
      closeResultType = event.resultType,
      assignee = None,
      payload = patchPayloadIfExists(existingTask, event.payloadUpdate)
    )
  }
}
