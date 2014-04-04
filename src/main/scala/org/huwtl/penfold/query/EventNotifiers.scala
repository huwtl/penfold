package org.huwtl.penfold.query

class EventNotifiers(notifiers: List[EventNotifier]) {
  def notifyAllOfEvents() {
    notifiers.foreach(_.notifyListener())
  }
}
