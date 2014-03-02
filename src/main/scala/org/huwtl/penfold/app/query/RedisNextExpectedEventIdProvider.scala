package org.huwtl.penfold.app.query

import com.redis.RedisClient
import org.huwtl.penfold.query.{EventSequenceId, NextExpectedEventIdProvider}

class RedisNextExpectedEventIdProvider(redisClient: RedisClient) extends NextExpectedEventIdProvider {
  val queryEventsStore = "queryEventsStore"

  override def nextExpectedEvent = {
    EventSequenceId(redisClient.scard(queryEventsStore) getOrElse 0L)
  }
}
