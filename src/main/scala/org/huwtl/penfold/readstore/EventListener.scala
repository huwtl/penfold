package org.huwtl.penfold.readstore

import org.huwtl.penfold.domain.event.Event

trait EventListener {
  def handle(event: Event): Boolean
}
