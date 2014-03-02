package org.huwtl.penfold.app.query

import org.huwtl.penfold.app.support.json.EventSerializer
import com.redis.RedisClient
import org.huwtl.penfold.query.{EventSequenceId, EventRecord, EventStoreQueryRepository}

class RedisEventStoreQueryRepository(redisClient: RedisClient, serializer: EventSerializer) extends EventStoreQueryRepository {
  private val eventStore = "events"

  override def retrieveIdOfLast = redisClient.hlen(eventStore).collect {
    case numOfEvents if numOfEvents > 0 => EventSequenceId(numOfEvents - 1)
  }

  override def retrieveBy(id: EventSequenceId) = redisClient.hget(eventStore, id.value).map {
    eventData => EventRecord(id, serializer.deserialize(eventData))
  }
}
