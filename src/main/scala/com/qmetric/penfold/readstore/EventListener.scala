package com.qmetric.penfold.readstore

import com.qmetric.penfold.domain.event.Event

trait EventListener {
  def handle(event: Event)
}
