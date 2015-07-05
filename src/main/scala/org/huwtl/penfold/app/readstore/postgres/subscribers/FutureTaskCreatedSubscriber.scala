package org.huwtl.penfold.app.readstore.postgres.subscribers

import org.huwtl.penfold.app.readstore.postgres.TaskData
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.event.{Event, FutureTaskCreated}
import org.huwtl.penfold.domain.model.Status.Waiting

class FutureTaskCreatedSubscriber extends TaskCreationSubscriber[FutureTaskCreated] {

  override def applicable(event: Event) = event.isInstanceOf[FutureTaskCreated]

  override def handleEvent(event: FutureTaskCreated, objectSerializer: ObjectSerializer) {
      val queue = event.queue

      val task = TaskData(
        event.aggregateId,
        event.aggregateVersion,
        event.created.getMillis,
        queue,
        Waiting,
        event.created.getMillis,
        previousStatus = None,
        attempts = 0,
        event.triggerDate.getMillis,
        assignee = None,
        event.score,
        event.triggerDate.getMillis,
        event.payload,
        rescheduleReason = None,
        cancelReason = None,
        closeReason = None,
        closeResultType = None
      )

      createNewTask(task, objectSerializer)
  }
}
