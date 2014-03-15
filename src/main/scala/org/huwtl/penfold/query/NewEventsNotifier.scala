package org.huwtl.penfold.query

class NewEventsNotifier(newEventsProvider: NewEventsProvider, eventListener: NewEventListener) {
  def notifyListener() {
    newEventsProvider.newEvents foreach {
      newEvent =>
        try {
          eventListener.handle(newEvent)
        }
        catch {
          case e: Exception => println(e)
        }
    }
  }
}
