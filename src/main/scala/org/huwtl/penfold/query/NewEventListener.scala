package org.huwtl.penfold.query

trait NewEventListener {
  def handle(newEvent: EventRecord): Boolean
}
