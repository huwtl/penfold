package com.qmetric.penfold.app.readstore.postgres.subscribers

import com.qmetric.penfold.app.readstore.postgres.TaskData
import com.qmetric.penfold.domain.event.{Event, TaskRescheduled}
import com.qmetric.penfold.domain.model.Status.Waiting

class TaskRescheduledSubscriber extends TaskUpdateSubscriber[TaskRescheduled] {

  override def applicable(event: Event) = event.isInstanceOf[TaskRescheduled]

  override def handleUpdateEvent(event: TaskRescheduled, existingTask: TaskData) = {
    existingTask.copy(
      previousStatus = Some(updatePreviousStatus(existingTask)),
      status = Waiting,
      statusLastModified = event.created.getMillis,
      score = event.score.getOrElse(existingTask.score),
      sort = event.triggerDate.getMillis,
      triggerDate = event.triggerDate.getMillis,
      rescheduleReason = event.reason,
      assignee = event.assignee,
      payload = patchPayloadIfExists(existingTask, event.payloadUpdate)
    )
  }
}
