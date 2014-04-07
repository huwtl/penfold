package org.huwtl.penfold.app.readstore.redis

import org.huwtl.penfold.support.RedisSpecification
import org.specs2.specification.Scope
import org.huwtl.penfold.readstore.EventSequenceId

class RedisNextExpectedEventIdProviderTest extends RedisSpecification {

  class context extends Scope {
    val trackingKey = "trackingKey"
    val redisClientPool = newRedisClientPool()
    val nextExpectedEventIdProvider = new RedisNextExpectedEventIdProvider(redisClientPool, trackingKey)
  }

  "provide id of next expected event to update into read store" in new context {
    nextExpectedEventIdProvider.nextExpectedEvent must beEqualTo(EventSequenceId(0))

    redisClientPool.withClient(_.set(trackingKey, "0"))
    nextExpectedEventIdProvider.nextExpectedEvent must beEqualTo(EventSequenceId(1))

    redisClientPool.withClient(_.set(trackingKey, "1"))
    nextExpectedEventIdProvider.nextExpectedEvent must beEqualTo(EventSequenceId(2))
  }
}
