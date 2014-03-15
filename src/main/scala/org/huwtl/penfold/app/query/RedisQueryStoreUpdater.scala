package org.huwtl.penfold.app.query

import org.huwtl.penfold.domain.event._
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.domain.event.JobTriggered
import com.redis.RedisClientPool
import org.joda.time.format.DateTimeFormat
import org.huwtl.penfold.domain.model.Status._
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.model.{QueueName, AggregateId, Status, Payload}
import org.huwtl.penfold.query.{EventSequenceId, EventRecord, NewEventListener}

class RedisQueryStoreUpdater(redisClientPool: RedisClientPool, objectSerializer: ObjectSerializer, keyFactory: RedisKeyFactory) extends NewEventListener {
  private val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  private val eventTrackerKey = keyFactory.eventTrackerKey("query")

  private val success = true

  private val trackEvent =
    """
      | local tracker = KEYS[1]
      | local eventId = ARGV[1]
      | if redis.call('sadd', tracker, eventId) == 0 then
      |   return '0'
      | end
    """.stripMargin

  lazy private val createJobScript = redisClientPool.withClient(_.scriptLoad(
    s"""
      | $trackEvent
      |
      | local jobKey = KEYS[2]
      | local queueKey = KEYS[3]
      | local statusKey = KEYS[4]
      | local aggregateId = ARGV[2]
      | local created = ARGV[3]
      | local queue = ARGV[4]
      | local status = ARGV[5]
      | local trigger = ARGV[6]
      | local payload = ARGV[7]
      | local score = ARGV[8]
      |
      | redis.call('hset', jobKey, 'created', created)
      | redis.call('hset', jobKey, 'queue', queue)
      | redis.call('hset', jobKey, 'status', status)
      | redis.call('hset', jobKey, 'trigger', trigger)
      | redis.call('hset', jobKey, 'score', score)
      | redis.call('hset', jobKey, 'payload', payload)
      |
      | redis.call('zadd', statusKey, score, aggregateId)
      | redis.call('zadd', queueKey, score, aggregateId)
      |
      | return '1'
    """.stripMargin
  ))

  lazy private val updateJobStatusScript = redisClientPool.withClient(_.scriptLoad(
    s"""
      | $trackEvent
      |
      | local jobKey = KEYS[2]
      | local oldQueueKey = KEYS[3]
      | local newQueueKey = KEYS[4]
      | local oldStatusKey = KEYS[5]
      | local newStatusKey = KEYS[6]
      | local aggregateId = ARGV[2]
      | local newStatus = ARGV[3]
      | local score = ARGV[4]
      |
      | redis.call('hset', jobKey, 'status', newStatus)
      |
      | redis.call('zrem', oldStatusKey, aggregateId)
      | redis.call('zrem', oldQueueKey, aggregateId)
      | redis.call('zadd', newStatusKey, score, aggregateId)
      | redis.call('zadd', newQueueKey, score, aggregateId)
      |
      | return '1'
    """.stripMargin
  ))

  override def handle(eventRecord: EventRecord) = {
    val eventId = eventRecord.id
    val aggregateId = eventRecord.event.aggregateId
    val jobKey = keyFactory.jobKey(aggregateId)

    eventRecord.event match {
      case e: JobCreated => {
        val statusKey = keyFactory.statusKey(Waiting)
        val queueKey = keyFactory.queueKey(e.queueName, Waiting)
        val payloadJson = objectSerializer.serialize[Payload](e.payload)
        redisClientPool.withClient {
          client =>
            client.evalSHA(createJobScript.get,
              keys = List(eventTrackerKey, jobKey, queueKey, statusKey),
              args = List(eventId.value, aggregateId.value, dateFormatter.print(e.created), e.queueName.value,
                Waiting.name, dateFormatter.print(e.triggerDate), payloadJson, e.triggerDate.getMillis))
        }
      }
      case e: JobTriggered => updateJobStatus(jobKey, eventId, aggregateId, Triggered)
      case e: JobStarted => updateJobStatus(jobKey, eventId, aggregateId, Started)
      case e: JobCompleted => updateJobStatus(jobKey, eventId, aggregateId, Completed)
      case e: JobCancelled => updateJobStatus(jobKey, eventId, aggregateId, Cancelled)
      case _ =>
    }

    success
  }

  private def updateJobStatus(jobKey: String, eventId: EventSequenceId, aggregateId: AggregateId, newStatus: Status) {
    redisClientPool.withClient {
      client =>
        val queueName = QueueName(client.hget(jobKey, "queue").get)
        val oldStatus = Status.from(client.hget(jobKey, "status").get).get
        val score = client.hget(jobKey, "score").get
        val oldQueueKey = keyFactory.queueKey(queueName, oldStatus)
        val oldStatusKey = keyFactory.statusKey(oldStatus)
        val newQueueKey = keyFactory.queueKey(queueName, newStatus)
        val newStatusKey = keyFactory.statusKey(newStatus)

        client.evalSHA(updateJobStatusScript.get,
          keys = List(eventTrackerKey, jobKey, oldQueueKey, newQueueKey, oldStatusKey, newStatusKey),
          args = List(eventId.value, aggregateId.value, newStatus.name, score))
    }
  }
}
