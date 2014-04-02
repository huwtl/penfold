package org.huwtl.penfold.app.query.redis

import com.redis.RedisClientPool
import org.huwtl.penfold.query.{EventTracker, EventSequenceId}

class RedisEventTracker(trackerKey: String, redisClientPool: RedisClientPool) extends EventTracker {
  val checkNotAlreadyHandledGuardScript =
    """
      | local tracker = KEYS[1]
      | local eventId = tonumber(ARGV[1])
      | local lastTrackedEventId = tonumber(redis.call('get', tracker))
      | if lastTrackedEventId ~= nil and lastTrackedEventId >= eventId then
      |   return '0'
      | end
    """.stripMargin

  lazy private val trackEventScript = redisClientPool.withClient(_.scriptLoad(
    s"""
      | $checkNotAlreadyHandledGuardScript
      | redis.call('set', tracker, eventId)
      | return '1'
    """.stripMargin
  ))

  override def trackEvent(eventId: EventSequenceId) = {
    redisClientPool.withClient(client =>
      client.evalSHA(trackEventScript.get, keys = List(trackerKey), args = List(eventId.value))
    )
  }
}
