package com.qmetric.penfold.readstore

trait EventListener {
  def handle(event: EventRecord): Boolean
}
