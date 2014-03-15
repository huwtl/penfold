package org.huwtl.penfold.query

class NewEventsPublisher(notifiers: List[NewEventsNotifier]) {
  def publishNewEvents() {
    notifiers.foreach(_.notifyListener())
  }
}
