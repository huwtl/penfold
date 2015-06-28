package com.qmetric.penfold.app.readstore.postgres.subscribers

import com.qmetric.penfold.app.readstore.postgres.TaskData
import com.qmetric.penfold.domain.event.{Event, TaskTriggered}
import com.qmetric.penfold.domain.model.Status.Ready

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
