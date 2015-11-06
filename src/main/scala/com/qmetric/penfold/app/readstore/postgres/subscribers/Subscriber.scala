package com.qmetric.penfold.app.readstore.postgres.subscribers

import com.qmetric.penfold.app.support.json.ObjectSerializer
import com.qmetric.penfold.domain.event.Event

trait Subscriber[E <: Event] {
  def applicable(event: Event): Boolean
  def handleEvent(event: E, objectSerializer: ObjectSerializer)
}
