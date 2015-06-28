package com.qmetric.penfold.app.readstore.postgres.subscribers

import com.qmetric.penfold.app.readstore.postgres.TaskData
import com.qmetric.penfold.app.support.json.ObjectSerializer
import com.qmetric.penfold.domain.event.{Event, FutureTaskCreated}
import com.qmetric.penfold.domain.model.Status.Waiting

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
