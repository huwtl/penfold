package org.huwtl.penfold.app.query

import com.redis.RedisClientPool
import org.huwtl.penfold.query.{EventSequenceId, NextExpectedEventIdProvider}

class RedisNextExpectedEventIdProvider(redisClientPool: RedisClientPool, eventTrackingKey: String) extends NextExpectedEventIdProvider {
  override def nextExpectedEvent = {
    EventSequenceId(
      redisClientPool.withClient(_.scard(eventTrackingKey) getOrElse 0L)
    )
  }
}
