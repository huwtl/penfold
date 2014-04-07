package org.huwtl.penfold.app.readstore.redis

import org.huwtl.penfold.app.support.json.{ObjectSerializer, EventSerializer}
import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.specs2.specification.Scope
import java.util.UUID
import org.huwtl.penfold.support.RedisSpecification
import org.huwtl.penfold.domain.event.JobCompleted
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.event.JobTriggered
import org.huwtl.penfold.domain.event.JobStarted
import org.huwtl.penfold.readstore.{EventSequenceId, EventRecord, PageRequest}

class RedisReadStoreUpdaterTest extends RedisSpecification {

  trait context extends Scope {
    val redisClientPool = newRedisClientPool()
    val aggregateRootId = AggregateId(UUID.randomUUID().toString)
    val queueId = QueueId("type")
    val payload = Payload(Map("a" -> "123", "b" -> 1))
    val created = new DateTime(2014, 2, 22, 12, 0, 0, 0)
    val triggerDate = new DateTime(2014, 2, 22, 12, 30, 0, 0)
    val pageRequest = PageRequest(0, 10)
    val serializer = new EventSerializer
    val index = Index("queue", List(IndexField("queue", "queues"), IndexField("status", "status")))
    val readStore = new RedisReadStore(redisClientPool, Indexes(List(index), redisKeyFactory), new ObjectSerializer, redisKeyFactory)
    val readStoreUpdater = new RedisReadStoreUpdater(redisClientPool, new ObjectSerializer, redisKeyFactory)
    val readStoreIndexUpdater = new RedisIndexUpdater(index, redisClientPool, new ObjectSerializer, redisKeyFactory)
  }

  "update read store on event" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, AggregateVersion(1), created, Binding(List(BoundQueue(queueId))), triggerDate, payload)
    val jobTriggeredEvent = JobTriggered(aggregateRootId, AggregateVersion(2), created, List(queueId))
    val jobStartedEvent = JobStarted(aggregateRootId, AggregateVersion(3), created, queueId)
    val jobCompletedEvent = JobCompleted(aggregateRootId, AggregateVersion(4), created, queueId)

    readStoreUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))
    readStoreIndexUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))
    readStore.retrieveByQueue(queueId, Status.Waiting, pageRequest).jobs.size must beEqualTo(1)

    readStoreUpdater.handle(EventRecord(EventSequenceId(2), jobTriggeredEvent))
    readStoreIndexUpdater.handle(EventRecord(EventSequenceId(2), jobTriggeredEvent))
    readStore.retrieveByQueue(queueId, Status.Waiting, pageRequest).jobs must beEmpty
    readStore.retrieveByQueue(queueId, Status.Ready, pageRequest).jobs.size must beEqualTo(1)

    readStoreUpdater.handle(EventRecord(EventSequenceId(3), jobStartedEvent))
    readStoreIndexUpdater.handle(EventRecord(EventSequenceId(3), jobStartedEvent))
    readStore.retrieveByQueue(queueId, Status.Ready, pageRequest).jobs must beEmpty
    readStore.retrieveByQueue(queueId, Status.Started, pageRequest).jobs.size must beEqualTo(1)

    readStoreUpdater.handle(EventRecord(EventSequenceId(4), jobCompletedEvent))
    readStoreIndexUpdater.handle(EventRecord(EventSequenceId(4), jobCompletedEvent))
    readStore.retrieveByQueue(queueId, Status.Started, pageRequest).jobs must beEmpty
    readStore.retrieveByQueue(queueId, Status.Completed, pageRequest).jobs.size must beEqualTo(1)
  }

  "do not persist events that have already been persisted" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, AggregateVersion(1), created, Binding(List(BoundQueue(queueId))), triggerDate, payload)
    val jobTriggeredEvent = JobTriggered(aggregateRootId, AggregateVersion(2), created, List(queueId))
    val jobStartedEvent = JobStarted(aggregateRootId, AggregateVersion(3), created, queueId)
    val jobCompletedEvent = JobCompleted(aggregateRootId, AggregateVersion(4), created, queueId)

    readStoreUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))
    readStoreIndexUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))
    readStore.retrieveByQueue(queueId, Status.Waiting, pageRequest).jobs.size must beEqualTo(1)
    readStoreUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))
    readStoreIndexUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))
    readStore.retrieveByQueue(queueId, Status.Waiting, pageRequest).jobs.size must beEqualTo(1)
  }
}
