package org.huwtl.penfold.app.readstore.postgres.subscribers

import org.huwtl.penfold.domain.event.Event

class Subscribers {
  private val subscribers = List(
    new TaskCreatedSubscriber,
    new FutureTaskCreatedSubscriber,
    new TaskStartedSubscriber,
    new TaskTriggeredSubscriber,
    new TaskRequeuedSubscriber,
    new TaskRescheduledSubscriber,
    new TaskUnassignedSubscriber,
    new TaskPayloadUpdatedSubscriber,
    new TaskClosedSubscriber,
    new TaskCancelledSubscriber,
    new TaskArchivedSubscriber
  )

  def findSuitable(event: Event) = subscribers
    .find(_.applicable(event))
    .asInstanceOf[Option[Subscriber[Event]]]
}
