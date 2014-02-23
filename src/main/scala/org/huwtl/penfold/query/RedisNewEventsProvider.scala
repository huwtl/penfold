package org.huwtl.penfold.query

import com.redis.RedisClient
import org.huwtl.penfold.app.support.json.EventSerializer
import org.huwtl.penfold.domain.model.Id

class RedisNewEventsProvider(redisClient: RedisClient, serializer: EventSerializer) extends NewEventsProvider {
  private val eventStore = "events"

  private val queryEventsStore = "queryEventsStore"

  override def newEvents = {
    val lastEventId = redisClient.hlen(eventStore) getOrElse 0L
    val nextExpectedEventId = (redisClient.scard(queryEventsStore) getOrElse 0L) + 1

    for {
      eventId <- (nextExpectedEventId to lastEventId).toStream
      eventData <- redisClient.hget(eventStore, eventId)
    } yield NewEvent(Id(eventId.toString), serializer.deserialize(eventData))
  }
}
