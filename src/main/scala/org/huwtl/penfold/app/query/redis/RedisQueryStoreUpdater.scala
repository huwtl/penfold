package org.huwtl.penfold.app.query.redis

import com.redis.RedisClientPool
import org.joda.time.format.DateTimeFormat
import org.huwtl.penfold.domain.model.Status._
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.query.EventListener
import org.huwtl.penfold.domain.event.JobCompleted
import org.huwtl.penfold.query.EventRecord
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.domain.event.JobTriggered
import org.huwtl.penfold.domain.event.JobCancelled
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.domain.event.JobStarted
import org.huwtl.penfold.query.EventSequenceId

class RedisQueryStoreUpdater(redisClientPool: RedisClientPool, objectSerializer: ObjectSerializer, keyFactory: RedisKeyFactory) extends EventListener {
  private val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  private val eventTrackerKey = keyFactory.eventTrackerKey("query")

  private val eventTracker = new RedisEventTracker(eventTrackerKey, redisClientPool)

  private val success = true

  lazy private val createJobScript = redisClientPool.withClient(_.scriptLoad(
    s"""
      | ${eventTracker.checkNotAlreadyHandledGuardScript}
      |
      | local jobKey = KEYS[2]
      | local created = ARGV[2]
      | local binding = ARGV[3]
      | local status = ARGV[4]
      | local triggerDate = ARGV[5]
      | local payload = ARGV[6]
      | local score = ARGV[7]
      |
      | redis.call('hset', jobKey, 'created', created)
      | redis.call('hset', jobKey, 'binding', binding)
      | redis.call('hset', jobKey, 'status', status)
      | redis.call('hset', jobKey, 'triggerDate', triggerDate)
      | redis.call('hset', jobKey, 'payload', payload)
      | redis.call('hset', jobKey, 'score', score)
      |
      | return '1'
    """.stripMargin
  ))

  lazy private val updateJobStatusScript = redisClientPool.withClient(_.scriptLoad(
    s"""
      | ${eventTracker.checkNotAlreadyHandledGuardScript}
      |
      | local jobKey = KEYS[2]
      | local status = ARGV[2]
      |
      | redis.call('hset', jobKey, 'status', status)
      |
      | return '1'
    """.stripMargin
  ))

  override def handle(eventRecord: EventRecord) = {
    val eventId = eventRecord.id
    val aggregateId = eventRecord.event.aggregateId
    val jobKey = keyFactory.jobKey(aggregateId)

    eventRecord.event match {
      case e: JobCreated => createJob(e, eventId, jobKey)
      case e: JobTriggered => updateJobStatus(jobKey, eventId, Ready)
      case e: JobStarted => updateJobStatus(jobKey, eventId, Started)
      case e: JobCompleted => updateJobStatus(jobKey, eventId, Completed)
      case e: JobCancelled => updateJobStatus(jobKey, eventId, Cancelled)
      case _ =>
    }

    eventTracker.trackEvent(eventId)

    success
  }

  private def createJob(event: JobCreated, eventId: EventSequenceId, jobKey: String) = {
    val payloadJson = objectSerializer.serialize[Payload](event.payload)
    val bindingJson = objectSerializer.serialize[Binding](event.binding)

    redisClientPool.withClient(client =>
      client.evalSHA(createJobScript.get,
        keys = List(eventTrackerKey, jobKey),
        args = List(eventId.value, dateFormatter.print(event.created), bindingJson,
          Waiting.name, dateFormatter.print(event.triggerDate), payloadJson, event.triggerDate.getMillis))
    )
  }

  private def updateJobStatus(jobKey: String, eventId: EventSequenceId, status: Status) {
    redisClientPool.withClient(client =>
      client.evalSHA(updateJobStatusScript.get,
        keys = List(eventTrackerKey, jobKey),
        args = List(eventId.value, status.name))
    )
  }
}
