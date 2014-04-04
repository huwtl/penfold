package org.huwtl.penfold.app.query.redis

import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.specs2.specification.Scope
import java.util.UUID
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.support.RedisSpecification
import org.specs2.matcher.DataTables
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.query.{EventSequenceId, PageRequest, EventRecord}

class RedisQueryRepositoryTest extends RedisSpecification with DataTables {
  val aggregateRootId = AggregateId(UUID.randomUUID().toString)

  val queueId = QueueId("type")

  val payload = Payload(Map("a" -> "123", "b" -> 1))

  val created = new DateTime(2014, 2, 22, 12, 0, 0, 0)

  val triggerDate = new DateTime(2014, 2, 22, 12, 30, 0, 0)

  val status = Status.Waiting

  class context extends Scope {
    val redisClientPool = newRedisClientPool()
    val queueIndex = Index("queue", List(IndexField("queue", "queues"), IndexField("status", "status")))
    val statusIndex = Index("status", List(IndexField("status", "status")))
    val queryRepositoryUpdater = new RedisQueryStoreUpdater(redisClientPool, new ObjectSerializer, redisKeyFactory)
    val queueIndexUpdater = new RedisIndexUpdater(queueIndex, redisClientPool, new ObjectSerializer, redisKeyFactory)
    val statusIndexUpdater = new RedisIndexUpdater(statusIndex, redisClientPool, new ObjectSerializer, redisKeyFactory)
    val queryRepository = new RedisQueryRepository(redisClientPool, Indexes(List(queueIndex, statusIndex), redisKeyFactory), new ObjectSerializer, redisKeyFactory)
  }

  "retrieve job by id" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, AggregateVersion.init, created, Binding(List(BoundQueue(queueId))), triggerDate, payload)

    queryRepository.retrieveBy(aggregateRootId) must beNone
    queryRepositoryUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))
    queryRepository.retrieveBy(aggregateRootId) must not(beNone)
  }

  "retrieve jobs by queue" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, AggregateVersion.init, created, Binding(List(BoundQueue(queueId))), triggerDate, payload)

    queryRepository.retrieveByQueue(queueId, status, PageRequest(0, 10)).jobs must beEmpty
    queryRepositoryUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))
    queueIndexUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))
    queryRepository.retrieveByQueue(queueId, status, PageRequest(0, 10)).jobs.size must beEqualTo(1)
  }

  "retrieve jobs by page" in new context {
    createJobEvent(AggregateId("a1"), EventSequenceId(1), queryRepositoryUpdater, queueIndexUpdater)
    createJobEvent(AggregateId("a2"), EventSequenceId(2), queryRepositoryUpdater, queueIndexUpdater)
    createJobEvent(AggregateId("a3"), EventSequenceId(3), queryRepositoryUpdater, queueIndexUpdater)
    createJobEvent(AggregateId("a4"), EventSequenceId(4), queryRepositoryUpdater, queueIndexUpdater)
    createJobEvent(AggregateId("a5"), EventSequenceId(5), queryRepositoryUpdater, queueIndexUpdater)

    "pageRequest"        | "expected"                         | "prevPage" | "nextPage" |
    PageRequest(0, 10)   ! List("a5", "a4", "a3", "a2", "a1") ! false      ! false      |
    PageRequest(0, 1)    ! List("a5")                         ! false      ! true       |
    PageRequest(2, 2)    ! List("a1")                         ! true       ! false      |
    PageRequest(3, 2)    ! List()                             ! true       ! false      |
    PageRequest(9, 2)    ! List()                             ! true       ! false      |
    PageRequest(0, 0)    ! List()                             ! false      ! true       |
    PageRequest(1, 1)    ! List("a4")                         ! true       ! true       |
    PageRequest(1, 2)    ! List("a3", "a2")                   ! true       ! true       |> {
      (pageRequest, expected, prevPage, nextPage) =>
        val pageResult = queryRepository.retrieveByQueue(queueId, status, pageRequest)
        pageResult.jobs.map(_.id) must beEqualTo(expected.map(AggregateId))
        pageResult.previousExists must beEqualTo(prevPage)
        pageResult.nextExists must beEqualTo(nextPage)
    }
  }

  "retrieve jobs ready to queue" in new context {
    val readyToTriggerJobCreatedEvent = JobCreated(aggregateRootId, AggregateVersion.init, created, Binding(List(BoundQueue(queueId))), DateTime.now().minusHours(1), payload)
    val notReadyToTriggerJobCreatedEvent = JobCreated(aggregateRootId, AggregateVersion.init, created, Binding(List(BoundQueue(queueId))), DateTime.now().plusHours(1), payload)

    queryRepository.retrieveJobsToQueue must beEmpty
    queryRepositoryUpdater.handle(EventRecord(EventSequenceId(1), notReadyToTriggerJobCreatedEvent))
    statusIndexUpdater.handle(EventRecord(EventSequenceId(1), notReadyToTriggerJobCreatedEvent))
    queryRepository.retrieveJobsToQueue must beEmpty
    queryRepositoryUpdater.handle(EventRecord(EventSequenceId(2), readyToTriggerJobCreatedEvent))
    statusIndexUpdater.handle(EventRecord(EventSequenceId(2), readyToTriggerJobCreatedEvent))
    queryRepository.retrieveJobsToQueue.size must beEqualTo(1)
  }

  private def createJobEvent(aggregateId: AggregateId, eventId: EventSequenceId, queryRepositoryUpdater: RedisQueryStoreUpdater, indexUpdater: RedisIndexUpdater) = {
    val event = JobCreated(aggregateId, AggregateVersion.init, created, Binding(List(BoundQueue(queueId))), triggerDate, payload)
    queryRepositoryUpdater.handle(EventRecord(eventId, event))
    indexUpdater.handle(EventRecord(eventId, event))
    event
  }
}
