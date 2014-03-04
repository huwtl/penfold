package org.huwtl.penfold.app.query

import com.redis.RedisClientPool
import org.huwtl.penfold.query.{EventSequenceId, NextExpectedEventIdProvider}

class RedisNextExpectedEventIdProvider(redisClientPool: RedisClientPool) extends NextExpectedEventIdProvider {
  val queryEventsStore = "queryEventsStore"

  override def nextExpectedEvent = {
    EventSequenceId(
      redisClientPool.withClient(_.scard(queryEventsStore) getOrElse 0L)
    )
  }
}
