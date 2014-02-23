package org.huwtl.penfold.query

import org.huwtl.penfold.domain.event._
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.domain.event.JobTriggered
import com.redis.RedisClient
import org.joda.time.format.DateTimeFormat
import org.huwtl.penfold.domain.model.Status._
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.model.Payload

class RedisQueryStoreEventPersister(redisClient: RedisClient, objectSerializer: ObjectSerializer) extends NewEventHandler {
  val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  val queryStoreEventsKey = "queryStoreEvents"

  val updateQueryStoreEvents =
    """
      | local queryStoreEvents = KEYS[1]
      | local eventId = ARGV[1]
      | if redis.call('sadd', queryStoreEvents, eventId) == 0 then
      |   return '0'
      | end
    """.stripMargin

  val createJobScript = redisClient.scriptLoad(
    s"""
      | $updateQueryStoreEvents
      |
      | local jobKey = KEYS[2]
      | local status = KEYS[3]
      | local aggregateId = ARGV[2]
      | local created = ARGV[3]
      | local type = ARGV[4]
      | local trigger = ARGV[5]
      | local triggerEpoch = ARGV[6]
      | local payload = ARGV[7]
      |
      | redis.call('hset', jobKey, 'created', created)
      | redis.call('hset', jobKey, 'type', type)
      | redis.call('hset', jobKey, 'status', status)
      | redis.call('hset', jobKey, 'trigger', trigger)
      | redis.call('hset', jobKey, 'trigger.epoch', triggerEpoch)
      | redis.call('hset', jobKey, 'payload', payload)
      |
      | redis.call('zadd', status, triggerEpoch, aggregateId)
      |
      | return '1'
    """.stripMargin
  )

  val updateJobStatusScript = redisClient.scriptLoad(
    s"""
      | $updateQueryStoreEvents
      |
      | local jobKey = KEYS[2]
      | local oldStatus = KEYS[3]
      | local newStatus = KEYS[4]
      | local aggregateId = ARGV[2]
      |
      | local triggerEpoch = redis.call('hget', jobKey, 'trigger.epoch')
      |
      | redis.call('hset', jobKey, 'status', newStatus)
      |
      | redis.call('zrem', oldStatus, aggregateId)
      | redis.call('zadd', newStatus, triggerEpoch, aggregateId)
      |
      | return '1'
    """.stripMargin
  )

  override def handle(newEvent: NewEvent) {
    val eventId = newEvent.id.value
    val aggregateId = newEvent.event.aggregateId.value
    val jobKey = s"job:$aggregateId"

    newEvent.event match {
      case e: JobCreated => {
        val payloadJson = objectSerializer.serialize[Payload](e.payload)
        redisClient.evalSHA(createJobScript.get, List(queryStoreEventsKey, jobKey, Waiting.name), List(eventId, aggregateId, dateFormatter.print(e.created), e.jobType.value, dateFormatter.print(e.triggerDate), e.triggerDate.getMillis, payloadJson))
      }
      case e: JobTriggered => redisClient.evalSHA(updateJobStatusScript.get, List(queryStoreEventsKey, jobKey, Waiting.name, Triggered.name), List(eventId, aggregateId))
      case e: JobStarted => redisClient.evalSHA(updateJobStatusScript.get, List(queryStoreEventsKey, jobKey, Triggered.name, Started.name), List(eventId, aggregateId))
      case e: JobCompleted => redisClient.evalSHA(updateJobStatusScript.get, List(queryStoreEventsKey, jobKey, Started.name, Completed.name), List(eventId, aggregateId))
      case e: JobCancelled => redisClient.evalSHA(updateJobStatusScript.get, List(queryStoreEventsKey, jobKey, Waiting.name, Cancelled.name), List(eventId, aggregateId))
      case _ =>
    }
  }
}
