package org.huwtl.penfold.app.readstore.postgres.subscribers

import org.huwtl.penfold.app.readstore.postgres.TaskData
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.event.{Event, TaskCreated}
import org.huwtl.penfold.domain.model.Status.Ready

class TaskCreatedSubscriber extends TaskCreationSubscriber[TaskCreated] {

  override def applicable(event: Event) = event.isInstanceOf[TaskCreated]

  override def handleEvent(event: TaskCreated, objectSerializer: ObjectSerializer) {
      val queue = event.queue

      val task = TaskData(
        event.aggregateId,
        event.aggregateVersion,
        event.created.getMillis,
        queue,
        Ready,
        event.created.getMillis,
        previousStatus = None,
        attempts = 0,
        event.triggerDate.getMillis,
        assignee = None,
        event.score,
        event.score,
        event.payload,
        rescheduleReason = None,
        cancelReason = None,
        closeReason = None,
        closeResultType = None
      )

      createNewTask(task, objectSerializer)
  }
}
