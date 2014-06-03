package com.qmetric.penfold.readstore

class EventNotifiers(notifiers: List[EventNotifier]) {
  def notifyAllOfEvents() {
    notifiers.foreach(_.notifyListener())
  }
}
