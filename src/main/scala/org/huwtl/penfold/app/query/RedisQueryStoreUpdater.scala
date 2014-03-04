package org.huwtl.penfold.app.query

import org.huwtl.penfold.domain.event._
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.domain.event.JobTriggered
import com.redis.RedisClientPool
import org.joda.time.format.DateTimeFormat
import org.huwtl.penfold.domain.model.Status._
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.model.{AggregateId, Status, Payload}
import org.huwtl.penfold.query.{EventSequenceId, EventRecord, NewEventListener}

class RedisQueryStoreUpdater(redisClientPool: RedisClientPool, objectSerializer: ObjectSerializer) extends NewEventListener {
  private val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  private val queryStoreEventsKey = "queryEventsStore"

  private val success = true

  private val trackQueryStoreEvent =
    """
      | local queryStoreEvents = KEYS[1]
      | local eventId = ARGV[1]
      | if redis.call('sadd', queryStoreEvents, eventId) == 0 then
      |   return '0'
      | end
    """.stripMargin

  lazy private val createJobScript = redisClientPool.withClient(_.scriptLoad(
    s"""
      | $trackQueryStoreEvent
      |
      | local jobKey = KEYS[2]
      | local queueKey = KEYS[3]
      | local status = KEYS[4]
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
      | redis.call('zadd', queueKey, triggerEpoch, aggregateId)
      |
      | return '1'
    """.stripMargin
  ))

  lazy private val updateJobStatusScript = redisClientPool.withClient(_.scriptLoad(
    s"""
      | $trackQueryStoreEvent
      |
      | local jobKey = KEYS[2]
      | local oldQueueKey = KEYS[3]
      | local newQueueKey = KEYS[4]
      | local oldStatus = KEYS[5]
      | local newStatus = KEYS[6]
      | local aggregateId = ARGV[2]
      |
      | local triggerEpoch = redis.call('hget', jobKey, 'trigger.epoch')
      |
      | redis.call('hset', jobKey, 'status', newStatus)
      |
      | redis.call('zrem', oldStatus, aggregateId)
      | redis.call('zrem', oldQueueKey, aggregateId)
      | redis.call('zadd', newStatus, triggerEpoch, aggregateId)
      | redis.call('zadd', newQueueKey, triggerEpoch, aggregateId)
      |
      | return '1'
    """.stripMargin
  ))

  override def handle(eventRecord: EventRecord) = {
    val eventId = eventRecord.id
    val aggregateId = eventRecord.event.aggregateId
    val jobKey = s"job:${aggregateId.value}"

    eventRecord.event match {
      case e: JobCreated => {
        val queueKey = s"${e.queueName.value}.${Waiting.name}"
        val payloadJson = objectSerializer.serialize[Payload](e.payload)
        redisClientPool.withClient {
          client =>
            client.evalSHA(createJobScript.get,
              List(queryStoreEventsKey, jobKey, queueKey, Waiting.name),
              List(eventId.value, aggregateId.value, dateFormatter.print(e.created), e.queueName.value,
                dateFormatter.print(e.triggerDate), e.triggerDate.getMillis, payloadJson))
        }
      }
      case e: JobTriggered => updateJobStatus(jobKey, eventId, aggregateId, Triggered)
      case e: JobStarted => updateJobStatus(jobKey, eventId, aggregateId, Started)
      case e: JobCompleted => updateJobStatus(jobKey, eventId, aggregateId, Completed)
      case e: JobCancelled => updateJobStatus(jobKey, eventId, aggregateId, Cancelled)
      case _ => throw new RuntimeException(s"Unhandled event $eventRecord")
    }

    success
  }

  private def updateJobStatus(jobKey: String, eventId: EventSequenceId, aggregateId: AggregateId, newStatus: Status) {
    redisClientPool.withClient {
      client =>
        val queueName = client.hget(jobKey, "type").get
        val oldStatus = client.hget(jobKey, "status").get
        val oldQueueKey = s"$queueName.$oldStatus"
        val newQueueKey = s"$queueName.${newStatus.name}"

        client.evalSHA(updateJobStatusScript.get,
          List(queryStoreEventsKey, jobKey, oldQueueKey, newQueueKey, oldStatus, newStatus.name),
          List(eventId.value, aggregateId.value))
    }
  }
}
