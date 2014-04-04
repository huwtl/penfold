package org.huwtl.penfold.query

trait EventListener {
  def handle(event: EventRecord): Boolean
}
