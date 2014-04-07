package org.huwtl.penfold.readstore

trait EventListener {
  def handle(event: EventRecord): Boolean
}
