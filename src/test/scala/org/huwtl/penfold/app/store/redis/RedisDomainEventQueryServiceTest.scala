package org.huwtl.penfold.app.store.redis

import org.specs2.specification.Scope
import org.huwtl.penfold.app.support.json.EventSerializer
import org.huwtl.penfold.domain.event.JobTriggered
import org.huwtl.penfold.domain.model.{AggregateVersion, AggregateId}
import org.huwtl.penfold.support.RedisSpecification
import org.huwtl.penfold.query.{EventSequenceId, EventRecord}
import org.joda.time.DateTime

class RedisDomainEventQueryServiceTest extends RedisSpecification {

  class context extends Scope {
    val created = new DateTime(2014, 3, 1, 12, 0, 0, 0)
    val eventSerializer = new EventSerializer
    val redisClientPool = newRedisClientPool()
    val redisEventStore = new RedisEventStore(redisClientPool, eventSerializer)
    val queryService = new RedisDomainEventQueryService(redisClientPool, eventSerializer)
  }

  "retrieve nothing for last id when domain event store is empty" in new context {
    queryService.retrieveIdOfLast must beNone
  }

  "retrieve id of last event added to domain event store" in new context {
    val event1 = JobTriggered(AggregateId("a1"), AggregateVersion.init, created, List())
    val event2 = JobTriggered(AggregateId("a2"), AggregateVersion.init, created, List())
    redisEventStore.add(event1)
    redisEventStore.add(event2)

    queryService.retrieveIdOfLast must beEqualTo(Some(EventSequenceId(1)))
  }

  "retrieve event from domain event store" in new context {
    val event1 = JobTriggered(AggregateId("a1"), AggregateVersion.init, created, List())
    val event2 = JobTriggered(AggregateId("a2"), AggregateVersion.init, created, List())
    redisEventStore.add(event1)
    redisEventStore.add(event2)

    queryService.retrieveBy(EventSequenceId(0)) must beEqualTo(Some(new EventRecord(EventSequenceId(0), event1)))
    queryService.retrieveBy(EventSequenceId(2)) must beNone
  }
}
