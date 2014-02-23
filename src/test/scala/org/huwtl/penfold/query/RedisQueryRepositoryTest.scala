package org.huwtl.penfold.query

import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.specs2.specification.Scope
import java.util.UUID
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.support.RedisSpecification
import org.huwtl.penfold.domain.model.Id
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.domain.model.JobType

class RedisQueryRepositoryTest extends RedisSpecification {
  val aggregateRootId = Id(UUID.randomUUID().toString)

  val jobType = JobType("type")

  val payload = Payload(Map("a" -> "123", "b" -> 1))

  val created = new DateTime(2014, 2, 22, 12, 0, 0, 0)

  val triggerDate = new DateTime(2014, 2, 22, 12, 30, 0, 0)

  class context extends Scope {
    val redisClient = newRedisClient()
    val queryRepositoryUpdater = new RedisQueryStoreEventPersister(redisClient, new ObjectSerializer)
    val queryRepository = new RedisQueryRepository(redisClient, new ObjectSerializer)
  }

  "retrieve job by id" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, Version.init, jobType, created, triggerDate, payload)

    queryRepository.retrieveBy(aggregateRootId) must beNone
    queryRepositoryUpdater.handle(NewEvent(Id("1"), jobCreatedEvent))
    queryRepository.retrieveBy(aggregateRootId) must not(beNone)
  }

  "retrieve jobs by status" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, Version.init, jobType, created, triggerDate, payload)

    queryRepository.retrieveBy(Status.Waiting) must beEmpty
    queryRepositoryUpdater.handle(NewEvent(Id("1"), jobCreatedEvent))
    queryRepository.retrieveBy(Status.Waiting).size must beEqualTo(1)
  }

  "retrieve jobs ready to trigger" in new context {
    val triggeredJobCreatedEvent = JobCreated(aggregateRootId, Version.init, jobType, created, DateTime.now().minusHours(1), payload)
    val untriggeredJobCreatedEvent = JobCreated(aggregateRootId, Version.init, jobType, created, DateTime.now().plusHours(1), payload)

    queryRepository.retrieveWithPendingTrigger must beEmpty
    queryRepositoryUpdater.handle(NewEvent(Id("1"), untriggeredJobCreatedEvent))
    queryRepository.retrieveWithPendingTrigger must beEmpty
    queryRepositoryUpdater.handle(NewEvent(Id("2"), triggeredJobCreatedEvent))
    queryRepository.retrieveWithPendingTrigger.size must beEqualTo(1)
  }
}
