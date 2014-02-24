package org.huwtl.penfold.query

import org.huwtl.penfold.app.support.json.{ObjectSerializer, EventSerializer}
import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.specs2.specification.Scope
import java.util.UUID
import org.huwtl.penfold.support.RedisSpecification
import org.huwtl.penfold.domain.event.JobCompleted
import org.huwtl.penfold.domain.model.Id
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.domain.model.QueueName
import org.huwtl.penfold.domain.event.JobTriggered
import org.huwtl.penfold.domain.event.JobStarted

class RedisQueryStoreEventPersisterTest extends RedisSpecification {
  trait context extends Scope {
    val aggregateRootId = Id(UUID.randomUUID().toString)
    val redisClient = newRedisClient()
    val serializer = new EventSerializer
    val queryRepository = new RedisQueryRepository(redisClient, new ObjectSerializer)
    val queryStoreUpdater = new RedisQueryStoreEventPersister(redisClient, new ObjectSerializer)
  }

  "update query store on event" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, Version(1), QueueName("type"), new DateTime(2014, 2, 22, 12, 0, 0, 0), new DateTime(2014, 2, 22, 12, 30, 0, 0), Payload(Map("a" -> "123", "b" -> 1)))
    val jobTriggeredEvent = JobTriggered(aggregateRootId, Version(2))
    val jobStartedEvent = JobStarted(aggregateRootId, Version(3))
    val jobCompletedEvent = JobCompleted(aggregateRootId, Version(4))

    queryStoreUpdater.handle(NewEvent(Id("1"), jobCreatedEvent))
    queryRepository.retrieveBy(Status.Waiting).size must beEqualTo(1)

    queryStoreUpdater.handle(NewEvent(Id("2"), jobTriggeredEvent))
    queryRepository.retrieveBy(Status.Waiting) must beEmpty
    queryRepository.retrieveBy(Status.Triggered).size must beEqualTo(1)

    queryStoreUpdater.handle(NewEvent(Id("3"), jobStartedEvent))
    queryRepository.retrieveBy(Status.Triggered) must beEmpty
    queryRepository.retrieveBy(Status.Started).size must beEqualTo(1)

    queryStoreUpdater.handle(NewEvent(Id("4"), jobCompletedEvent))
    queryRepository.retrieveBy(Status.Started) must beEmpty
    queryRepository.retrieveBy(Status.Completed).size must beEqualTo(1)
  }

  "do not persist events that have already been persisted" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, Version(1), QueueName("type"), new DateTime(2014, 2, 22, 12, 0, 0, 0), new DateTime(2014, 2, 22, 12, 30, 0, 0), Payload(Map("a" -> "123", "b" -> 1)))
    val jobTriggeredEvent = JobTriggered(aggregateRootId, Version(2))
    val jobStartedEvent = JobStarted(aggregateRootId, Version(3))
    val jobCompletedEvent = JobCompleted(aggregateRootId, Version(4))

    queryStoreUpdater.handle(NewEvent(Id("1"), jobCreatedEvent))
    queryRepository.retrieveBy(Status.Waiting).size must beEqualTo(1)
    queryStoreUpdater.handle(NewEvent(Id("1"), jobCreatedEvent))
    queryRepository.retrieveBy(Status.Waiting).size must beEqualTo(1)
  }
}
