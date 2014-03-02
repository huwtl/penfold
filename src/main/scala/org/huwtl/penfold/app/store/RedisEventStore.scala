package org.huwtl.penfold.app.store

import org.huwtl.penfold.domain.store.EventStore
import org.huwtl.penfold.domain.model.AggregateId
import com.redis.RedisClient
import org.huwtl.penfold.app.support.json.EventSerializer
import org.huwtl.penfold.domain.event.Event
import org.huwtl.penfold.domain.exceptions.EventConflictException
import scala.util.{Success, Failure, Try}

class RedisEventStore(redisClient: RedisClient, eventSerializer: EventSerializer) extends EventStore {
  val eventStore = "events"

  val conflictError = "CONFLICT"

  val script = redisClient.scriptLoad(
    s"""
      | local eventStore = KEYS[1]
      | local aggregateId = KEYS[2]
      | local event = ARGV[1]
      | local expectedVersion = tonumber(ARGV[2])
      |
      | local eventId = redis.call('hlen', eventStore)
      |
      | local version = tonumber(redis.call('llen', aggregateId)) + 1
      | if expectedVersion ~= version then
      |   return redis.error_reply('$conflictError')
      | end
      |
      | redis.call('hset', eventStore, eventId, event)
      | redis.call('rpush', aggregateId, eventId)
      |
      | return 'OK'
    """.stripMargin
  )

  override def add(event: Event) = {
    val serialized = eventSerializer.serialize(event)

    Try(redisClient.evalSHA(script.get, List(eventStore, event.aggregateId.value), List(serialized, event.aggregateVersion.number))) match {
      case Failure(e) if e.getMessage.contains(conflictError) => throw new EventConflictException(s"event conflict ${event.aggregateId}")
      case Success(value) =>
    }
  }

  override def retrieveBy(id: AggregateId) = {
    val events = for {
      optEventId <- redisClient.lrange(id.value, 0, -1).get
      eventId <- optEventId
      event <- redisClient.hget(eventStore, eventId)
    } yield eventSerializer.deserialize(event)
    events
  }
}
