package org.huwtl.penfold.app.readstore.postgres.subscribers

import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.event.Event

trait Subscriber[E <: Event] {
  def applicable(event: Event): Boolean
  def handleEvent(event: E, objectSerializer: ObjectSerializer)
}
