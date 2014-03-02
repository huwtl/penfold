package org.huwtl.penfold.domain.model

import org.joda.time.DateTime
import org.huwtl.penfold.domain.event._
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.domain.event.JobTriggered

object Job extends AggregateFactory {
  def create(aggregateId: AggregateId, queueName: QueueName, payload: Payload) = {
    applyJobCreated(JobCreated(aggregateId, Version.init, queueName, DateTime.now(), DateTime.now(), payload))
  }

  def create(aggregateId: AggregateId, queueName: QueueName, triggerDate: DateTime, payload: Payload) = {
    applyJobCreated(JobCreated(aggregateId, Version.init, queueName, DateTime.now(), triggerDate, payload))
  }

  def applyEvent = {
    case event: JobCreated => applyJobCreated(event)
    case event => unhandled(event)
  }

  private def applyJobCreated(event: JobCreated) = ScheduledJob(
    event :: Nil,
    event.aggregateId,
    event.aggregateVersion,
    event.created,
    event.queueName,
    Status.Waiting,
    event.triggerDate,
    event.payload
  )
}

sealed trait Job extends AggregateRoot

case class ScheduledJob(uncommittedEvents: List[Event],
                        aggregateId: AggregateId,
                        version: Version,
                        created: DateTime,
                        queueName: QueueName,
                        status: Status,
                        triggerDate: DateTime,
                        payload: Payload) extends Job {

  def trigger(): ScheduledJob = {
    require(status == Status.Waiting, "Can only trigger a waiting job")
    applyJobTriggered(JobTriggered(aggregateId, version.next))
  }

  def start(): ScheduledJob = {
    require(status == Status.Triggered, "Can only start a triggered job")
    applyJobStarted(JobStarted(aggregateId, version.next))
  }

  def cancel(): CancelledJob = {
    applyJobCancelled(JobCancelled(aggregateId, version.next))
  }

  def complete(): CompletedJob = {
    require(status == Status.Started, "Can only complete a started job")
    applyJobCompleted(JobCompleted(aggregateId, version.next))
  }

  def markCommitted = copy(uncommittedEvents = Nil)

  def applyEvent = {
    case event: JobTriggered => applyJobTriggered(event)
    case event: JobStarted => applyJobStarted(event)
    case event: JobCancelled => applyJobCancelled(event)
    case event: JobCompleted => applyJobCompleted(event)
    case event => unhandled(event)
  }

  private def applyJobTriggered(event: JobTriggered) = copy(event :: uncommittedEvents, version = version.next, status = Status.Triggered)

  private def applyJobStarted(event: JobStarted) = copy(event :: uncommittedEvents, version = version.next, status = Status.Started)

  private def applyJobCancelled(event: JobCancelled) = CancelledJob(event :: uncommittedEvents, event.aggregateId, version = version.next, Status.Cancelled)

  private def applyJobCompleted(event: JobCompleted) = CompletedJob(event :: uncommittedEvents, event.aggregateId, version = version.next, Status.Completed)
}

case class CompletedJob(uncommittedEvents: List[Event], aggregateId: AggregateId, version: Version, status: Status) extends Job {
  def markCommitted = copy(uncommittedEvents = Nil)

  def applyEvent = unhandled
}

case class CancelledJob(uncommittedEvents: List[Event], aggregateId: AggregateId, version: Version, status: Status) extends Job {
  def markCommitted = copy(uncommittedEvents = Nil)

  def applyEvent = unhandled
}