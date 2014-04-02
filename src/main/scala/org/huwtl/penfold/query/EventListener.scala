package org.huwtl.penfold.query

trait EventListener {
  def handle(newEvent: EventRecord): Boolean
}
