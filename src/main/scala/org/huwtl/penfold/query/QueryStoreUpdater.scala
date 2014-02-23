package org.huwtl.penfold.query

class QueryStoreUpdater(newEventsProvider: NewEventsProvider, newEventHandler: NewEventHandler) {
  def updateWithNewEvents() {
    val newEventsStream = newEventsProvider.newEvents
    newEventsStream.foreach(newEventHandler.handle)
  }
}
