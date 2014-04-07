package org.huwtl.penfold.app.readstore.redis

import org.huwtl.penfold.support.RedisSpecification
import org.specs2.specification.Scope
import org.huwtl.penfold.domain.model._
import java.util.UUID
import org.joda.time.DateTime
import org.huwtl.penfold.app.support.json.{ObjectSerializer, EventSerializer}
import org.huwtl.penfold.readstore._
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.readstore.EventRecord
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.readstore.PageRequest
import org.huwtl.penfold.domain.event.{JobTriggered, JobCreated}
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.readstore.EventSequenceId

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
    val readStore = new RedisReadStore(redisClientPool, Indexes(List(index), redisKeyFactory), new ObjectSerializer, redisKeyFactory)
    val readStoreUpdater = new RedisReadStoreUpdater(redisClientPool, new ObjectSerializer, redisKeyFactory)
    val readStoreIndexUpdater = new RedisIndexUpdater(index, redisClientPool, new ObjectSerializer, redisKeyFactory)
  }

  "create new index" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, AggregateVersion(1), created, Binding(List(boundQueue)), triggerDate, payload)

    readStoreUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))
    readStoreIndexUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))

    readStore.retrieveBy(Filters(List(Filter("status", "waiting"), Filter("field1", "123"), Filter("field2", "1"))), pageRequest).jobs.size must beEqualTo(1)
    readStore.retrieveBy(Filters(List(Filter("status", "waiting"), Filter("field1", "123"), Filter("field2", "2"))), pageRequest).jobs must beEmpty
    readStore.retrieveBy(Filters(List(Filter("status", "waiting"), Filter("field1", "123"))), pageRequest).jobs must beEmpty
    readStore.retrieveBy(Filters(List(Filter("status", "waiting"), Filter("field1", "123"), Filter("field2", ""))), pageRequest).jobs must beEmpty
  }

  "update existing index" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, AggregateVersion(1), created, Binding(List(boundQueue)), triggerDate, payload)
    val jobTriggeredEvent = JobTriggered(aggregateRootId, AggregateVersion(2), created, List(queueId))

    readStoreUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))
    readStoreIndexUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))

    readStore.retrieveBy(Filters(List(Filter("status", "waiting"), Filter("field1", "123"), Filter("field2", "1"))), pageRequest).jobs.size must beEqualTo(1)
    readStore.retrieveBy(Filters(List(Filter("status", "ready"), Filter("field1", "123"), Filter("field2", "1"))), pageRequest).jobs must beEmpty

    readStoreUpdater.handle(EventRecord(EventSequenceId(2), jobTriggeredEvent))
    readStoreIndexUpdater.handle(EventRecord(EventSequenceId(2), jobTriggeredEvent))

    readStore.retrieveBy(Filters(List(Filter("status", "waiting"), Filter("field1", "123"), Filter("field2", "1"))), pageRequest).jobs must beEmpty
    readStore.retrieveBy(Filters(List(Filter("status", "ready"), Filter("field1", "123"), Filter("field2", "1"))), pageRequest).jobs.size must beEqualTo(1)
  }
}
