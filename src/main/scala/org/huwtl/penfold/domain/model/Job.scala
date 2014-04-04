package org.huwtl.penfold.domain.model

import org.joda.time.DateTime
import org.joda.time.DateTime.now
import org.huwtl.penfold.domain.event._
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.domain.model.Status._

object Job extends AggregateFactory {
  def create(aggregateId: AggregateId, binding: Binding, payload: Payload) = {
    applyJobCreated(JobCreated(aggregateId, AggregateVersion.init, now, binding, now, payload))
  }

  def create(aggregateId: AggregateId, binding: Binding, triggerDate: DateTime, payload: Payload) = {
    applyJobCreated(JobCreated(aggregateId, AggregateVersion.init, now, binding, triggerDate, payload))
  }

  def applyEvent = {
    case event: JobCreated => applyJobCreated(event)
    case event => unhandled(event)
  }

  private def applyJobCreated(event: JobCreated) = Job(
    event :: Nil,
    event.aggregateId,
    event.aggregateVersion,
    event.created,
    event.binding,
    Waiting,
    event.triggerDate,
    event.payload
  )
}

case class Job(uncommittedEvents: List[Event],
                        aggregateId: AggregateId,
                        version: AggregateVersion,
                        created: DateTime,
                        binding: Binding,
                        status: Status,
                        triggerDate: DateTime,
                        payload: Payload) extends AggregateRoot {

  override def aggregateType = AggregateType.Job

  def trigger(): Job = {
    require(status == Waiting, s"Can only queue a waiting job but was $status")
    applyJobTriggered(JobTriggered(aggregateId, version.next, now, binding.queues.map(_.id)))
  }

  def start(queue: QueueId): Job = {
    require(status == Ready, s"Can only start a job that is ready but was $status")
    applyJobStarted(JobStarted(aggregateId, version.next, now, queue))
  }

  def cancel(queue: QueueId): Job = {
    applyJobCancelled(JobCancelled(aggregateId, version.next, now, List(queue)))
  }

  def complete(queue: QueueId): Job = {
    require(status == Started, s"Can only complete a started job but was $status")
    applyJobCompleted(JobCompleted(aggregateId, version.next, now, queue))
  }

  def markCommitted = copy(uncommittedEvents = Nil)

  def applyEvent = {
    case event: JobTriggered => applyJobTriggered(event)
    case event: JobStarted => applyJobStarted(event)
    case event: JobCancelled => applyJobCancelled(event)
    case event: JobCompleted => applyJobCompleted(event)
    case event => unhandled(event)
  }

  private def applyJobTriggered(event: JobTriggered) = copy(event :: uncommittedEvents, version = version.next, status = Ready)

  private def applyJobStarted(event: JobStarted) = copy(event :: uncommittedEvents, version = version.next, status = Started)

  private def applyJobCancelled(event: JobCancelled) = copy(event :: uncommittedEvents, event.aggregateId, version = version.next, status = Cancelled)

  private def applyJobCompleted(event: JobCompleted) = copy(event :: uncommittedEvents, event.aggregateId, version = version.next, status = Completed)
}