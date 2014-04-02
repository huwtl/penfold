package org.huwtl.penfold.app.query.redis

import org.huwtl.penfold.support.RedisSpecification
import org.specs2.specification.Scope
import org.huwtl.penfold.domain.model._
import java.util.UUID
import org.joda.time.DateTime
import org.huwtl.penfold.app.support.json.{ObjectSerializer, EventSerializer}
import org.huwtl.penfold.query._
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.query.EventRecord
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.query.PageRequest
import org.huwtl.penfold.domain.event.{JobTriggered, JobCreated}
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.query.EventSequenceId

class RedisIndexUpdaterTest extends RedisSpecification {
  trait context extends Scope {
    val redisClientPool = newRedisClientPool()
    val aggregateRootId = AggregateId(UUID.randomUUID().toString)
    val queueId = QueueId("type")
    val boundQueue = BoundQueue(queueId)
    val payload = Payload(Map("field1" -> "123", "inner" -> Map("field2" -> 1)))
    val created = new DateTime(2014, 2, 22, 12, 0, 0, 0)
    val triggerDate = new DateTime(2014, 2, 22, 12, 30, 0, 0)
    val pageRequest = PageRequest(0, 10)
    val serializer = new EventSerializer
    val index = Index("test", List(
      IndexField("status", "status"),
      IndexField("field1", "payload / field1"),
      IndexField("field2", "payload / inner / field2"))
    )
    val queryRepository = new RedisQueryRepository(redisClientPool, Indexes(List(index), redisKeyFactory), new ObjectSerializer, redisKeyFactory)
    val queryStoreUpdater = new RedisQueryStoreUpdater(redisClientPool, new ObjectSerializer, redisKeyFactory)
    val queryIndexUpdater = new RedisIndexUpdater(index, redisClientPool, new ObjectSerializer, redisKeyFactory)
  }

  "create new index" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, Version(1), created, Binding(List(boundQueue)), triggerDate, payload)

    queryStoreUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))
    queryIndexUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))

    queryRepository.retrieveBy(Filters(List(Filter("status", "waiting"), Filter("field1", "123"), Filter("field2", "1"))), pageRequest).jobs.size must beEqualTo(1)
    queryRepository.retrieveBy(Filters(List(Filter("status", "waiting"), Filter("field1", "123"), Filter("field2", "2"))), pageRequest).jobs must beEmpty
    queryRepository.retrieveBy(Filters(List(Filter("status", "waiting"), Filter("field1", "123"))), pageRequest).jobs must beEmpty
    queryRepository.retrieveBy(Filters(List(Filter("status", "waiting"), Filter("field1", "123"), Filter("field2", ""))), pageRequest).jobs must beEmpty
  }

  "update existing index" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, Version(1), created, Binding(List(boundQueue)), triggerDate, payload)
    val jobTriggeredEvent = JobTriggered(aggregateRootId, Version(2), created, List(queueId))

    queryStoreUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))
    queryIndexUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))

    queryRepository.retrieveBy(Filters(List(Filter("status", "waiting"), Filter("field1", "123"), Filter("field2", "1"))), pageRequest).jobs.size must beEqualTo(1)
    queryRepository.retrieveBy(Filters(List(Filter("status", "ready"), Filter("field1", "123"), Filter("field2", "1"))), pageRequest).jobs must beEmpty

    queryStoreUpdater.handle(EventRecord(EventSequenceId(2), jobTriggeredEvent))
    queryIndexUpdater.handle(EventRecord(EventSequenceId(2), jobTriggeredEvent))

    queryRepository.retrieveBy(Filters(List(Filter("status", "waiting"), Filter("field1", "123"), Filter("field2", "1"))), pageRequest).jobs must beEmpty
    queryRepository.retrieveBy(Filters(List(Filter("status", "ready"), Filter("field1", "123"), Filter("field2", "1"))), pageRequest).jobs.size must beEqualTo(1)
  }
}
