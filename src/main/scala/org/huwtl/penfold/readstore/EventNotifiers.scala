package org.huwtl.penfold.readstore

class EventNotifiers(notifiers: List[EventNotifier]) {
  def notifyAllOfEvents() {
    notifiers.foreach(_.notifyListener())
  }
}
