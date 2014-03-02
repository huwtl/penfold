package org.huwtl.penfold.app.query

import org.specs2.specification.Scope
import org.huwtl.penfold.app.support.json.EventSerializer
import org.huwtl.penfold.app.store.RedisEventStore
import org.huwtl.penfold.domain.event.JobTriggered
import org.huwtl.penfold.domain.model.{Version, AggregateId}
import org.huwtl.penfold.support.RedisSpecification
import org.huwtl.penfold.query.{EventSequenceId, EventRecord}

class RedisEventStoreQueryServiceTest extends RedisSpecification {
  class context extends Scope {
    val eventSerializer = new EventSerializer
    val redisClient = newRedisClient()
    val redisEventStore = new RedisEventStore(redisClient, eventSerializer)
    val eventStoreQueryRepository = new RedisEventStoreQueryService(redisClient, eventSerializer)
  }

  "retrieve nothing for last id when domain event store is empty" in new context {
    eventStoreQueryRepository.retrieveIdOfLast must beNone
  }

  "retrieve id of last event added to domain event store" in new context {
    val event1 = JobTriggered(AggregateId("a0"), Version.init)
    val event2 = JobTriggered(AggregateId("a1"), Version.init)
    redisEventStore.add(event1)
    redisEventStore.add(event2)

    eventStoreQueryRepository.retrieveIdOfLast must beEqualTo(Some(EventSequenceId(1)))
  }

  "retrieve event from domain event store" in new context {
    val event1 = JobTriggered(AggregateId("a0"), Version.init)
    val event2 = JobTriggered(AggregateId("a1"), Version.init)
    redisEventStore.add(event1)
    redisEventStore.add(event2)

    eventStoreQueryRepository.retrieveBy(EventSequenceId(0)) must beEqualTo(Some(new EventRecord(EventSequenceId(0), event1)))
    eventStoreQueryRepository.retrieveBy(EventSequenceId(2)) must beNone
  }
}
