package org.huwtl.penfold.app.store.redis

import org.huwtl.penfold.domain.store.EventStore
import org.huwtl.penfold.domain.model.AggregateId
import com.redis.RedisClientPool
import org.huwtl.penfold.app.support.json.EventSerializer
import org.huwtl.penfold.domain.event.Event
import org.huwtl.penfold.domain.exceptions.EventConflictException
import scala.util.{Success, Failure, Try}

class RedisEventStore(redisPool: RedisClientPool, eventSerializer: EventSerializer) extends EventStore {
  val eventsKey = "events"

  val conflictError = "CONFLICT"

  lazy val script = redisPool.withClient(_.scriptLoad(
    s"""
      | local eventsKey = KEYS[1]
      | local aggregateEventsKey = KEYS[2]
      | local event = ARGV[1]
      | local expectedVersion = tonumber(ARGV[2])
      |
      | local eventId = redis.call('hlen', eventsKey)
      |
      | local version = tonumber(redis.call('llen', aggregateEventsKey)) + 1
      | if expectedVersion ~= version then
      |   return redis.error_reply('$conflictError')
      | end
      |
      | redis.call('hset', eventsKey, eventId, event)
      | redis.call('rpush', aggregateEventsKey, eventId)
      |
      | return 'OK'
    """.stripMargin
  ))

  override def add(event: Event) = {
    val serialized = eventSerializer.serialize(event)
    redisPool.withClient {
      client =>
        Try(client.evalSHA(script.get,
          keys = List(eventsKey, aggregateEventsKey(event.aggregateId)),
          args = List(serialized, event.aggregateVersion.number))) match {
          case Failure(e) if e.getMessage.contains(conflictError) => throw new EventConflictException(s"event conflict ${event.aggregateId}")
          case Success(value) =>
        }
    }
  }

  override def retrieveBy(id: AggregateId) = {
    redisPool.withClient {
      client =>
        val events = for {
          optEventId <- client.lrange(aggregateEventsKey(id), 0, -1).get
          eventId <- optEventId
          event <- client.hget(eventsKey, eventId)
        } yield eventSerializer.deserialize(event)
        events
    }
  }

  private def aggregateEventsKey(id: AggregateId) = {
    s"agg:${id.value}:events"
  }
}
