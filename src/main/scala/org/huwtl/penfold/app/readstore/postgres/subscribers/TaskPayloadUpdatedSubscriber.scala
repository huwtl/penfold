package org.huwtl.penfold.app.readstore.postgres.subscribers

import org.huwtl.penfold.app.readstore.postgres.TaskData
import org.huwtl.penfold.domain.event.{Event, TaskPayloadUpdated}
import org.huwtl.penfold.domain.model.Status.Ready

class TaskPayloadUpdatedSubscriber extends TaskUpdateSubscriber[TaskPayloadUpdated] {

  override def applicable(event: Event) = event.isInstanceOf[TaskPayloadUpdated]

  override def handleUpdateEvent(event: TaskPayloadUpdated, existingTask: TaskData) = {
    existingTask.copy(
      sort = if (existingTask.status == Ready) event.score getOrElse existingTask.sort else existingTask.sort,
      score = event.score getOrElse existingTask.score,
      payload = patchPayloadIfExists(existingTask, Some(event.payloadUpdate))
    )
  }
}
