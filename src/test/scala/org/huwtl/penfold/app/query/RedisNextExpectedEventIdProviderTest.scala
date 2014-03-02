package org.huwtl.penfold.app.query

import org.huwtl.penfold.support.RedisSpecification
import org.specs2.specification.Scope
import org.huwtl.penfold.query.EventSequenceId

class RedisNextExpectedEventIdProviderTest extends RedisSpecification {

  class context extends Scope {
    val redisClient = newRedisClient()
    val nextExpectedEventIdProvider = new RedisNextExpectedEventIdProvider(redisClient)
  }

  "provide id of next expected event to update into query store" in new context {
    nextExpectedEventIdProvider.nextExpectedEvent must beEqualTo(EventSequenceId(0))

    redisClient.sadd(nextExpectedEventIdProvider.queryEventsStore, "a")
    nextExpectedEventIdProvider.nextExpectedEvent must beEqualTo(EventSequenceId(1))

    redisClient.sadd(nextExpectedEventIdProvider.queryEventsStore, "b")
    nextExpectedEventIdProvider.nextExpectedEvent must beEqualTo(EventSequenceId(2))
  }
}
