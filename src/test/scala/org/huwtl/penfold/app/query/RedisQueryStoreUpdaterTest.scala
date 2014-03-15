package org.huwtl.penfold.app.query

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
import org.huwtl.penfold.domain.model.QueueName
import org.huwtl.penfold.domain.event.JobTriggered
import org.huwtl.penfold.domain.event.JobStarted
import org.huwtl.penfold.query.{EventSequenceId, EventRecord, PageRequest}

class RedisQueryStoreUpdaterTest extends RedisSpecification {

  trait context extends Scope {
    val redisClientPool = newRedisClientPool()
    val aggregateRootId = AggregateId(UUID.randomUUID().toString)
    val queueName = QueueName("type")
    val payload = Payload(Map("a" -> "123", "b" -> 1))
    val created = new DateTime(2014, 2, 22, 12, 0, 0, 0)
    val triggerDate = new DateTime(2014, 2, 22, 12, 30, 0, 0)
    val pageRequest = PageRequest(0, 10)
    val serializer = new EventSerializer
    val queryRepository = new RedisQueryRepository(redisClientPool, Indexes(Nil, redisKeyFactory), new ObjectSerializer, redisKeyFactory)
    val queryStoreUpdater = new RedisQueryStoreUpdater(redisClientPool, new ObjectSerializer, redisKeyFactory)
  }

  "update query store on event" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, Version(1), queueName, created, triggerDate, payload)
    val jobTriggeredEvent = JobTriggered(aggregateRootId, Version(2))
    val jobStartedEvent = JobStarted(aggregateRootId, Version(3))
    val jobCompletedEvent = JobCompleted(aggregateRootId, Version(4))

    queryStoreUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))
    queryRepository.retrieveBy(queueName, Status.Waiting, pageRequest).jobs.size must beEqualTo(1)

    queryStoreUpdater.handle(EventRecord(EventSequenceId(2), jobTriggeredEvent))
    queryRepository.retrieveBy(queueName, Status.Waiting, pageRequest).jobs must beEmpty
    queryRepository.retrieveBy(queueName, Status.Triggered, pageRequest).jobs.size must beEqualTo(1)

    queryStoreUpdater.handle(EventRecord(EventSequenceId(3), jobStartedEvent))
    queryRepository.retrieveBy(queueName, Status.Triggered, pageRequest).jobs must beEmpty
    queryRepository.retrieveBy(queueName, Status.Started, pageRequest).jobs.size must beEqualTo(1)

    queryStoreUpdater.handle(EventRecord(EventSequenceId(4), jobCompletedEvent))
    queryRepository.retrieveBy(queueName, Status.Started, pageRequest).jobs must beEmpty
    queryRepository.retrieveBy(queueName, Status.Completed, pageRequest).jobs.size must beEqualTo(1)
  }

  "do not persist events that have already been persisted" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, Version(1), queueName, created, triggerDate, payload)
    val jobTriggeredEvent = JobTriggered(aggregateRootId, Version(2))
    val jobStartedEvent = JobStarted(aggregateRootId, Version(3))
    val jobCompletedEvent = JobCompleted(aggregateRootId, Version(4))

    queryStoreUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))
    queryRepository.retrieveBy(queueName, Status.Waiting, pageRequest).jobs.size must beEqualTo(1)
    queryStoreUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))
    queryRepository.retrieveBy(queueName, Status.Waiting, pageRequest).jobs.size must beEqualTo(1)
  }
}
