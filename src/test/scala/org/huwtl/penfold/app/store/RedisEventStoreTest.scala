package org.huwtl.penfold.app.store

import org.specs2.mutable.Specification
import org.huwtl.penfold.app.support.json.EventSerializer
import org.huwtl.penfold.domain.model.{Version, AggregateId, QueueName, Payload}
import org.huwtl.penfold.domain.event.{JobTriggered, JobCreated}
import org.joda.time.DateTime
import org.specs2.specification.Scope
import java.util.UUID
import org.huwtl.penfold.support.RedisSpecification
import org.huwtl.penfold.domain.exceptions.EventConflictException

class RedisEventStoreTest extends Specification with RedisSpecification {

  trait context extends Scope {
    val aggregateRootId = AggregateId(UUID.randomUUID().toString)
    val queueName = QueueName("queue")
    val created = new DateTime(2014, 2, 22, 12, 0, 0, 0)
    val triggerDate = new DateTime(2014, 2, 22, 12, 30, 0, 0)
    val payload = Payload(Map("a" -> "123", "b" -> 1))
    val serializer = new EventSerializer
    val redisPool = newRedisClientPool()
    val redisEventStore = new RedisEventStore(redisPool, serializer)
  }

  "store events in event store" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, Version.init, queueName, created, triggerDate, payload)
    val jobTriggeredEvent = JobTriggered(aggregateRootId, jobCreatedEvent.aggregateVersion.next)

    redisEventStore.add(jobCreatedEvent)
    redisEventStore.add(jobTriggeredEvent)

    redisEventStore.retrieveBy(aggregateRootId) must beEqualTo(List(jobCreatedEvent, jobTriggeredEvent))
  }

  "prevent concurrent modifications to aggregate" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, Version.init, queueName, created, triggerDate, payload)
    val jobTriggeredEvent = JobTriggered(aggregateRootId, jobCreatedEvent.aggregateVersion)
    redisEventStore.add(jobCreatedEvent)

    redisEventStore.add(jobTriggeredEvent) must throwA[EventConflictException]
  }
}
