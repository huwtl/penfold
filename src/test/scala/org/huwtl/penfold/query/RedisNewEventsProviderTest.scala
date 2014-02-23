package org.huwtl.penfold.query

import org.specs2.specification.Scope
import org.huwtl.penfold.app.support.json.EventSerializer
import org.huwtl.penfold.app.store.RedisEventStore
import org.huwtl.penfold.domain.event.JobTriggered
import org.huwtl.penfold.domain.model.{Version, Id}
import org.huwtl.penfold.support.RedisSpecification

class RedisNewEventsProviderTest extends RedisSpecification {
  class context extends Scope {
    val eventSerializer = new EventSerializer
    val redisClient = newRedisClient()
    val redisEventStore = new RedisEventStore(redisClient, eventSerializer)
    val newEventsProvider = new RedisNewEventsProvider(redisClient, eventSerializer)
  }

  "provide new events" in new context {
    val event1 = JobTriggered(Id("1"), Version.init)
    val event2 = JobTriggered(Id("2"), Version.init)
    redisEventStore.add(event1)
    redisEventStore.add(event2)

    newEventsProvider.newEvents must beEqualTo(List(NewEvent(event1.aggregateId, event1), NewEvent(event2.aggregateId, event2)))
  }

  "provide nothing when no new events" in new context {
    newEventsProvider.newEvents must beEmpty
  }
}
