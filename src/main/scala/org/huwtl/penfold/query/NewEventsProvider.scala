package org.huwtl.penfold.query

trait NewEventsProvider {
  def newEvents: Stream[NewEvent]
}
