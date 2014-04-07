package org.huwtl.penfold.app.store.redis

import org.specs2.mutable.Specification
import org.huwtl.penfold.app.support.json.EventSerializer
import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.specs2.specification.Scope
import java.util.UUID
import org.huwtl.penfold.support.RedisSpecification
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.domain.event.JobTriggered
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.exceptions.AggregateConflictException

class RedisEventStoreTest extends Specification with RedisSpecification {

  trait context extends Scope {
    val aggregateRootId = AggregateId(UUID.randomUUID().toString)
    val queueId = QueueId("queue")
    val created = new DateTime(2014, 2, 22, 12, 0, 0, 0)
    val triggerDate = new DateTime(2014, 2, 22, 12, 30, 0, 0)
    val payload = Payload(Map("a" -> "123", "b" -> 1))
    val serializer = new EventSerializer
    val redisPool = newRedisClientPool()
    val store = new RedisEventStore(redisPool, serializer)
  }

  "store events" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, AggregateVersion.init, created, Binding(List(BoundQueue(queueId))), triggerDate, payload)
    val jobTriggeredEvent = JobTriggered(aggregateRootId, jobCreatedEvent.aggregateVersion.next, created, List(queueId))

    store.add(jobCreatedEvent)
    store.add(jobTriggeredEvent)

    store.retrieveBy(aggregateRootId) must beEqualTo(List(jobCreatedEvent, jobTriggeredEvent))
  }

  "prevent concurrent modifications to aggregate" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, AggregateVersion.init, created, Binding(List(BoundQueue(queueId))), triggerDate, payload)
    val jobTriggeredEvent = JobTriggered(aggregateRootId, jobCreatedEvent.aggregateVersion, created, List(queueId))
    store.add(jobCreatedEvent)

    store.add(jobTriggeredEvent) must throwA[AggregateConflictException]
  }

  "check connectivity to store" in new context {
    store.checkConnectivity.left.getOrElse(false) must beTrue
    redisPool.close
    store.checkConnectivity.isRight must beTrue
  }
}
