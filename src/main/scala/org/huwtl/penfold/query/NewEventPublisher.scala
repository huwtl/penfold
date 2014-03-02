package org.huwtl.penfold.query

class NewEventPublisher(newEventsProvider: NewEventsProvider, eventListeners: List[NewEventListener]) {
  def publishNewEvents() {
    newEventsProvider.newEvents.foreach { event =>
      eventListeners.foreach (_.handle(event))
    }
  }
}
