package org.huwtl.penfold.app.query.redis

import com.redis.RedisClientPool
import org.huwtl.penfold.query.{EventSequenceId, NextExpectedEventIdProvider}

class RedisNextExpectedEventIdProvider(redisClientPool: RedisClientPool, eventTrackingKey: String) extends NextExpectedEventIdProvider {
  override def nextExpectedEvent = {
    redisClientPool.withClient(client =>
      client.get[String](eventTrackingKey) match {
        case Some(lastEventId) => EventSequenceId(lastEventId.toLong + 1)
        case None => EventSequenceId.first
      }
    )
  }
}
