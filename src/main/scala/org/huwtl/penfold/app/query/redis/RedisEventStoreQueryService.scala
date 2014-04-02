package org.huwtl.penfold.app.query.redis

import org.huwtl.penfold.app.support.json.EventSerializer
import com.redis.RedisClientPool
import org.huwtl.penfold.query.{EventSequenceId, EventRecord, EventStoreQueryService}

class RedisEventStoreQueryService(redisClientPool: RedisClientPool, serializer: EventSerializer) extends EventStoreQueryService {
  private val eventStore = "events"

  override def retrieveIdOfLast = {
    redisClientPool.withClient(_.hlen(eventStore).collect {
      case numOfEvents if numOfEvents > 0 => EventSequenceId(numOfEvents - 1)
    })
  }

  override def retrieveBy(id: EventSequenceId) = {
    redisClientPool.withClient(_.hget(eventStore, id.value).map {
      eventData => EventRecord(id, serializer.deserialize(eventData))
    })
  }
}
