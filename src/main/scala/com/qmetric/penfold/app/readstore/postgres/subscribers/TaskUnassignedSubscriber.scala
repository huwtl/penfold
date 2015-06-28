package com.qmetric.penfold.app.readstore.postgres.subscribers

import com.qmetric.penfold.app.readstore.postgres.TaskData
import com.qmetric.penfold.domain.event.{Event, TaskUnassigned}

class TaskUnassignedSubscriber extends TaskUpdateSubscriber[TaskUnassigned] {

  override def applicable(event: Event) = event.isInstanceOf[TaskUnassigned]

  override def handleUpdateEvent(event: TaskUnassigned, existingTask: TaskData) = {
    existingTask.copy(
      assignee = None,
      payload = patchPayloadIfExists(existingTask, event.payloadUpdate)
    )
  }
}
