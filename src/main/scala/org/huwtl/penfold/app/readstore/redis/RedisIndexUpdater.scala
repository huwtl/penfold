package org.huwtl.penfold.app.readstore.redis

import com.redis.RedisClientPool
import org.huwtl.penfold.domain.model.Status._
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.readstore._
import org.huwtl.penfold.readstore.EventRecord
import org.huwtl.penfold.domain.event.JobTriggered
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.event.JobStarted
import org.huwtl.penfold.readstore.EventSequenceId
import org.huwtl.penfold.domain.event.JobCompleted
import org.huwtl.penfold.domain.event.JobCancelled

class RedisIndexUpdater(index: Index, redisClientPool: RedisClientPool, objectSerializer: ObjectSerializer, keyFactory: RedisKeyFactory) extends EventListener {
  private val eventTrackerKey = keyFactory.indexEventTrackerKey(index)

  private val eventTracker = new RedisEventTracker(eventTrackerKey, redisClientPool)

  private val success = true

  lazy private val idempotentUpdateJobScript = redisClientPool.withClient(_.scriptLoad(
    s"""
      | ${eventTracker.checkNotAlreadyHandledGuardScript}
      |
      | local jobKey = KEYS[2]
      | local search = ARGV[2]
      | local score = ARGV[3]
      |
      | redis.call('hset', jobKey, 'search', search)
      | redis.call('hset', jobKey, 'score', score)
      |
      | return '1'
    """.stripMargin
  ))

  lazy private val idempotentUpdateIndexScript = redisClientPool.withClient(_.scriptLoad(
    s"""
      | ${eventTracker.checkNotAlreadyHandledGuardScript}
      |
      | local indexKey = KEYS[2]
      | local aggregateId = ARGV[2]
      | local score = ARGV[3]
      |
      | redis.call('zadd', indexKey, score, aggregateId)
      |
      | return '1'
    """.stripMargin
  ))

  lazy private val idempotentRemoveFromIndexScript = redisClientPool.withClient(_.scriptLoad(
    s"""
      | ${eventTracker.checkNotAlreadyHandledGuardScript}
      |
      | local indexKey = KEYS[2]
      | local aggregateId = ARGV[2]
      |
      | redis.call('zrem', indexKey, aggregateId)
      |
      | return '1'
    """.stripMargin
  ))

  override def handle(eventRecord: EventRecord) = {
    val eventId = eventRecord.id
    val aggregateId = eventRecord.event.aggregateId

    eventRecord.event match {
      case e: JobCreated => handleCreateEvent(e, eventId, aggregateId)
      case e: JobTriggered => handleUpdateStatusEvent(eventRecord, Ready, e.queues)
      case e: JobStarted => handleUpdateStatusEvent(eventRecord, Started, List(e.queue))
      case e: JobCompleted => handleUpdateStatusEvent(eventRecord, Completed, List(e.queue))
      case e: JobCancelled => handleUpdateStatusEvent(eventRecord, Cancelled, e.queues)
      case _ =>
    }

    eventTracker.trackEvent(eventId)

    success
  }

  private def handleCreateEvent(event: JobCreated, eventId: EventSequenceId, aggregateId: AggregateId) {
    val searchRecord = JobSearchRecord(event.binding.queues.map(_.id), Waiting, event.payload)

    updateIndex(eventId, aggregateId, "{}", searchRecord, event.triggerDate.getMillis.toString)
  }

  private def handleUpdateStatusEvent(eventRecord: EventRecord, newStatus: Status, queues: List[QueueId]) {
    redisClientPool.withClient {
      client =>
        val jobKey = keyFactory.indexedJobKey(index, eventRecord.event.aggregateId)

        client.hget(jobKey, "search") match {
          case Some(oldSearchRecordJson) => {
            val score = client.hget(jobKey, "score").getOrElse("0")
            val newSearchRecord = objectSerializer.deserialize[JobSearchRecord](oldSearchRecordJson).copy(status = newStatus, queues = queues)
            updateIndex(eventRecord.id, eventRecord.event.aggregateId, oldSearchRecordJson, newSearchRecord, score)
          }
          case None =>
        }
    }
  }

  private def updateIndex(eventId: EventSequenceId, aggregateId: AggregateId, oldSearchRecordJson: String, newSearchRecord: JobSearchRecord, score: String) {
    val newSearchRecordJson = objectSerializer.serialize[JobSearchRecord](newSearchRecord)

    val oldIndexKeys = keyFactory.indexKeys(index, oldSearchRecordJson)
    val newIndexKeys = keyFactory.indexKeys(index, newSearchRecordJson)

    if (!oldIndexKeys.isEmpty || !newIndexKeys.isEmpty) {
      execJobUpdate(eventId, aggregateId, newSearchRecordJson, score)

      newIndexKeys.diff(oldIndexKeys).foreach(indexKey => execIndexUpdate(eventId, aggregateId, indexKey, score))

      oldIndexKeys.diff(newIndexKeys).foreach(indexKey => execRemoveFromIndex(eventId, aggregateId, indexKey))
    }
  }

  private def execJobUpdate(eventId: EventSequenceId, aggregateId: AggregateId, searchRecordJson: String, score: String) = {
    val jobKey = keyFactory.indexedJobKey(index, aggregateId)

    redisClientPool.withClient(_.evalSHA(idempotentUpdateJobScript.get,
      keys = List(eventTrackerKey, jobKey),
      args = List(eventId.value, searchRecordJson, score)
    ))
  }

  private def execIndexUpdate(eventId: EventSequenceId, aggregateId: AggregateId, indexKey: String, score: String) = {
    redisClientPool.withClient(_.evalSHA(idempotentUpdateIndexScript.get,
      keys = List(eventTrackerKey, indexKey),
      args = List(eventId.value, aggregateId.value, score)
    ))
  }

  private def execRemoveFromIndex(eventId: EventSequenceId, aggregateId: AggregateId, indexKey: String) = {
    redisClientPool.withClient(_.evalSHA(idempotentRemoveFromIndexScript.get,
      keys = List(eventTrackerKey, indexKey),
      args = List(eventId.value, aggregateId.value)
    ))
  }
}
