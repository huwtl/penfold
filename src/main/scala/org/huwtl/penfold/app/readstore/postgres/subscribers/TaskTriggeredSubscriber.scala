package org.huwtl.penfold.app.readstore.postgres.subscribers

import org.huwtl.penfold.app.readstore.postgres.TaskData
import org.huwtl.penfold.domain.event.{Event, TaskTriggered}
import org.huwtl.penfold.domain.model.Status.Ready

class TaskTriggeredSubscriber extends TaskUpdateSubscriber[TaskTriggered] {

  override def applicable(event: Event) = event.isInstanceOf[TaskTriggered]

  override def handleUpdateEvent(event: TaskTriggered, existingTask: TaskData) = {
    existingTask.copy(
      previousStatus = Some(updatePreviousStatus(existingTask)),
      status = Ready,
      statusLastModified = event.created.getMillis,
      sort = existingTask.score
    )
  }
}
