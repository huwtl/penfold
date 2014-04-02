package org.huwtl.penfold.app.query.redis

import com.redis.RedisClientPool
import org.huwtl.penfold.domain.model._
import org.joda.time.format.DateTimeFormat
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.joda.time.DateTime._
import org.huwtl.penfold.query._
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.model.AggregateId
import scala.Some
import org.huwtl.penfold.query.JobRecordReference
import org.huwtl.penfold.query.PageRequest
import org.huwtl.penfold.query.JobRecord
import org.huwtl.penfold.query.PageResult
import org.huwtl.penfold.domain.model.Status.Waiting

class RedisQueryRepository(redisClientPool: RedisClientPool, indexes: Indexes, objectSerializer: ObjectSerializer, keyFactory: RedisKeyFactory) extends QueryRepository {
  private val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  lazy private val retrieveJobScript = redisClientPool.withClient(_.scriptLoad(
    """
      | local jobKey = KEYS[1]
      |
      | if redis.call('exists', jobKey) == 0 then
      |   return {}
      | end
      |
      | local created = redis.call('hget', jobKey, 'created')
      | local binding = redis.call('hget', jobKey, 'binding')
      | local status = redis.call('hget', jobKey, 'status')
      | local triggerDate = redis.call('hget', jobKey, 'triggerDate')
      | local payload = redis.call('hget', jobKey, 'payload')
      |
      | return {created, binding, status, triggerDate, payload}
    """.stripMargin
  ))

  override def retrieveByQueue(queueId: QueueId, status: Status, pageRequest: PageRequest, filters: Filters = Filters.empty) = {
    val filtersByQueueAndStatus = filters.copy(filters = Filter("queue", queueId.value) :: Filter("status", status.name) :: filters.all)
    retrieveBy(filtersByQueueAndStatus, pageRequest)
  }

  override def retrieveByStatus(status: Status, pageRequest: PageRequest, filters: Filters = Filters.empty) = {
    val filtersByStatus = filters.copy(filters = Filter("status", status.name) :: filters.all)
    retrieveBy(filtersByStatus, pageRequest)
  }

  override def retrieveBy(filters: Filters, pageRequest: PageRequest) = {
    indexes.keyFor(filters) match {
      case Some(indexKey) => retrievePage(indexKey, pageRequest)
      case None => PageResult.empty
    }
  }

  override def retrieveBy(aggregateId: AggregateId) = {
    val jobKeyName = keyFactory.jobKey(aggregateId)

    val jobAttributes = redisClientPool.withClient(client =>
      client.evalMultiSHA[String](retrieveJobScript.get, keys = List(jobKeyName), args = Nil).getOrElse(Nil)
    )

    if (jobAttributes.isEmpty) {
      None
    }
    else {
      val created = jobAttributes(0).get
      val binding = objectSerializer.deserialize[Binding](jobAttributes(1).get)
      val status = jobAttributes(2).get
      val triggerDate = jobAttributes(3).get
      val payload = jobAttributes(4).get

      Some(JobRecord(
        aggregateId,
        dateFormatter.parseDateTime(created),
        binding,
        Status.from(status).get,
        dateFormatter.parseDateTime(triggerDate),
        objectSerializer.deserialize[Payload](payload)))
    }
  }

  override protected def retrieveNextPageOfJobsToTrigger(pageRequest: PageRequest) = {
    val waitingIndexKey = indexes.keyFor(Filters(List(Filter("status", Waiting.name)))).get

    val nextPageOfJobsToTrigger = redisClientPool.withClient(client =>
      client.zrangebyscore(key = waitingIndexKey, max = now().getMillis, limit = Some(pageRequest.start, pageRequest.pageSize))
    )

    nextPageOfJobsToTrigger.getOrElse(Nil).map {
      aggregateId => new JobRecordReference(AggregateId(aggregateId))
    }
  }

  private def retrievePage(indexKey: String, pageRequest: PageRequest): PageResult = {
    val aggregateIdsWithOverflow = redisClientPool.withClient(_.zrange(indexKey, pageRequest.start, pageRequest.end).getOrElse(Nil))
    val aggregateIdsWithoutOverflow = aggregateIdsWithOverflow.take(pageRequest.pageSize)

    val previousPageExists = !pageRequest.isFirstPage
    val nextPageExists = aggregateIdsWithOverflow.size != aggregateIdsWithoutOverflow.size

    PageResult(
      pageRequest.pageNumber,
      aggregateIdsWithoutOverflow.map(id => retrieveBy(AggregateId(id)).get),
      previousPageExists,
      nextPageExists)
  }
}
