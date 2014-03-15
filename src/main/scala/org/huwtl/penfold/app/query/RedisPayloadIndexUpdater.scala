package org.huwtl.penfold.app.query

import com.redis.RedisClientPool
import org.huwtl.penfold.domain.model.Status._
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.model.Status
import org.huwtl.penfold.query._
import org.huwtl.penfold.domain.model.QueueName
import org.huwtl.penfold.query.EventRecord
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.event.JobStarted
import org.huwtl.penfold.query.EventSequenceId
import org.huwtl.penfold.domain.event.JobCompleted
import org.huwtl.penfold.domain.event.JobCancelled
import org.huwtl.penfold.domain.event.JobTriggered

class RedisPayloadIndexUpdater(index: Index, redisClientPool: RedisClientPool, objectSerializer: ObjectSerializer, keyFactory: RedisKeyFactory) extends NewEventListener {
  private val eventTrackerKey = keyFactory.indexEventTrackerKey(index)

  private val success = true

  private val checkNotAlreadyHandledEvent =
    """
      | local tracker = KEYS[1]
      | local eventId = ARGV[1]
      | if redis.call('sismember', tracker, eventId) == 1 then
      |   return '0'
      | end
    """.stripMargin

  lazy private val trackEventScript = redisClientPool.withClient(_.scriptLoad(
    """
      | local tracker = KEYS[1]
      | local eventId = ARGV[1]
      | if redis.call('sadd', tracker, eventId) == 0 then
      |   return '0'
      | end
    """.stripMargin
  ))

  lazy private val idempotentUpdateIndexScript = redisClientPool.withClient(_.scriptLoad(
    s"""
      | $checkNotAlreadyHandledEvent
      |
      | local indexKey = KEYS[2]
      | local oldQueueIndexKey = KEYS[3]
      | local newQueueIndexKey = KEYS[4]
      | local jobKey = KEYS[5]
      | local aggregateId = ARGV[2]
      | local queue = ARGV[3]
      | local status = ARGV[4]
      | local score = ARGV[5]
      | local payload = ARGV[6]
      |
      | redis.call('hset', jobKey, 'queue', queue)
      | redis.call('hset', jobKey, 'status', status)
      | redis.call('hset', jobKey, 'score', score)
      | redis.call('hset', jobKey, 'payload', payload)
      |
      | redis.call('zadd', indexKey, score, aggregateId)
      | redis.call('zrem', oldQueueIndexKey, aggregateId)
      | redis.call('zadd', newQueueIndexKey, score, aggregateId)
      |
      | return '1'
    """.stripMargin
  ))

  override def handle(eventRecord: EventRecord) = {
    val eventId = eventRecord.id
    val aggregateId = eventRecord.event.aggregateId

    eventRecord.event match {
      case e: JobCreated => handleCreateEvent(e, eventId, aggregateId)
      case e: JobTriggered => handleUpdateEvent(eventId, aggregateId, Triggered)
      case e: JobStarted => handleUpdateEvent(eventId, aggregateId, Started)
      case e: JobCompleted => handleUpdateEvent(eventId, aggregateId, Completed)
      case e: JobCancelled => handleUpdateEvent(eventId, aggregateId, Cancelled)
      case _ =>
    }

    markEventAsHandled(eventId)

    success
  }

  private def handleUpdateEvent(eventId: EventSequenceId, aggregateId: AggregateId, newStatus: Status) {
    redisClientPool.withClient {
      client =>
        val jobKey = keyFactory.indexJobKey(index, aggregateId)
        val queueName = client.hget(jobKey, "queue").get
        val oldStatus = client.hget(jobKey, "status").get
        val score = client.hget(jobKey, "score").get
        val payloadJson = client.hget(jobKey, "payload").get

        updateIndex(eventId, aggregateId, QueueName(queueName), Status.from(oldStatus).get, newStatus, payloadJson, score)
    }
  }

  private def handleCreateEvent(event: JobCreated, eventId: EventSequenceId, aggregateId: AggregateId) {
    val payloadJson = objectSerializer.serialize[Payload](event.payload)

    updateIndex(eventId, aggregateId, event.queueName, Waiting, Waiting, payloadJson, event.triggerDate.getMillis.toString)
  }

  private def updateIndex(eventId: EventSequenceId, aggregateId: AggregateId, queueName: QueueName, oldStatus: Status,
                          newStatus: Status, payloadJson: String, score: String) {
    val allJobIndexKeys = keyFactory.allJobsIndexKeys(index, payloadJson)

    allJobIndexKeys.foreach {
      allJobsIndexKey =>
        val oldQueueIndexKey = keyFactory.indexQueueKey(queueName, oldStatus, allJobsIndexKey)
        val newQueueIndexKey = keyFactory.indexQueueKey(queueName, newStatus, allJobsIndexKey)
        val jobKey = keyFactory.indexJobKey(index, aggregateId)

        redisClientPool.withClient(_.evalSHA(idempotentUpdateIndexScript.get,
          keys = List(eventTrackerKey, allJobsIndexKey, oldQueueIndexKey, newQueueIndexKey, jobKey),
          args = List(eventId.value, aggregateId.value, queueName.value, newStatus.name, score, payloadJson)
        ))
    }
  }

  private def markEventAsHandled(eventId: EventSequenceId) = {
    redisClientPool.withClient(_.evalSHA(trackEventScript.get, keys = List(eventTrackerKey), args = List(eventId.value)))
  }
}
