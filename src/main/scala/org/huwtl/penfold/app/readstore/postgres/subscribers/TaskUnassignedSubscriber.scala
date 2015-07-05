package org.huwtl.penfold.app.readstore.postgres.subscribers

import org.huwtl.penfold.app.readstore.postgres.TaskData
import org.huwtl.penfold.domain.event.{Event, TaskUnassigned}

class TaskUnassignedSubscriber extends TaskUpdateSubscriber[TaskUnassigned] {

  override def applicable(event: Event) = event.isInstanceOf[TaskUnassigned]

  override def handleUpdateEvent(event: TaskUnassigned, existingTask: TaskData) = {
    existingTask.copy(
      assignee = None,
      payload = patchPayloadIfExists(existingTask, event.payloadUpdate)
    )
  }
}
