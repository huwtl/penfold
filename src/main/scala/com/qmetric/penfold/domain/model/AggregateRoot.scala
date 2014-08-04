package com.qmetric.penfold.domain.model

import com.qmetric.penfold.domain.event.Event
import com.qmetric.penfold.domain.exceptions.AggregateConflictException

trait EventSourced {
  def applyEvent: Event => EventSourced

  def unhandled(event: Event) = sys.error("event " + event + " does not apply to " + this)
}

trait AggregateRoot extends EventSourced {
  def aggregateId : AggregateId

  def version: AggregateVersion

  def aggregateType: AggregateType

  def uncommittedEvents: List[Event]

  def markCommitted: AggregateRoot

  def checkVersion(expectedVersion: AggregateVersion) = {
    checkConflict(expectedVersion == version, s"aggregate conflict ${aggregateId}")
  }

  def checkConflict(requirement: Boolean, message: => Any) = if (!requirement) throw new AggregateConflictException("conflict: " + message)
}

trait AggregateFactory extends EventSourced {
  def loadFromHistory[T <: AggregateRoot](history: Iterable[Event]): T = {
    require(history.nonEmpty, "Can't load aggregate root without events")
    val eventSourced = history.tail.foldLeft(applyEvent(history.head))((older, newer) => older.applyEvent(newer))
    eventSourced.asInstanceOf[AggregateRoot].markCommitted.asInstanceOf[T]
  }
}