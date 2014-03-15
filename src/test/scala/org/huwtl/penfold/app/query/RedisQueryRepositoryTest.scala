package org.huwtl.penfold.app.query

import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.specs2.specification.Scope
import java.util.UUID
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.support.RedisSpecification
import org.specs2.matcher.DataTables
import org.huwtl.penfold.domain.model.QueueName
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.query.{EventSequenceId, PageRequest, EventRecord}

class RedisQueryRepositoryTest extends RedisSpecification with DataTables {
  val aggregateRootId = AggregateId(UUID.randomUUID().toString)

  val queueName = QueueName("type")

  val payload = Payload(Map("a" -> "123", "b" -> 1))

  val created = new DateTime(2014, 2, 22, 12, 0, 0, 0)

  val triggerDate = new DateTime(2014, 2, 22, 12, 30, 0, 0)

  val status = Status.Waiting

  class context extends Scope {
    val redisClientPool = newRedisClientPool()
    val queryRepositoryUpdater = new RedisQueryStoreUpdater(redisClientPool, new ObjectSerializer, redisKeyFactory)
    val queryRepository = new RedisQueryRepository(redisClientPool, Indexes(Nil, redisKeyFactory), new ObjectSerializer, redisKeyFactory)
  }

  "retrieve job by id" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, Version.init, queueName, created, triggerDate, payload)

    queryRepository.retrieveBy(aggregateRootId) must beNone
    queryRepositoryUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))
    queryRepository.retrieveBy(aggregateRootId) must not(beNone)
  }


  "retrieve jobs by status" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, Version.init, queueName, created, triggerDate, payload)

    queryRepository.retrieveBy(queueName, status, PageRequest(0, 10)).jobs must beEmpty
    queryRepositoryUpdater.handle(EventRecord(EventSequenceId(1), jobCreatedEvent))
    queryRepository.retrieveBy(queueName, status, PageRequest(0, 10)).jobs.size must beEqualTo(1)
  }

  "retrieve jobs by page" in new context {
    createJobEvent(AggregateId("a1"), EventSequenceId(1), queryRepositoryUpdater)
    createJobEvent(AggregateId("a2"), EventSequenceId(2), queryRepositoryUpdater)
    createJobEvent(AggregateId("a3"), EventSequenceId(3), queryRepositoryUpdater)
    createJobEvent(AggregateId("a4"), EventSequenceId(4), queryRepositoryUpdater)
    createJobEvent(AggregateId("a5"), EventSequenceId(5), queryRepositoryUpdater)

    "pageRequest"        | "expected"                         | "prevPage" | "nextPage" |
    PageRequest(0, 10)   ! List("a1", "a2", "a3", "a4", "a5") ! false      ! false      |
    PageRequest(0, 1)    ! List("a1")                         ! false      ! true       |
    PageRequest(2, 2)    ! List("a5")                         ! true       ! false      |
    PageRequest(3, 2)    ! List()                             ! true       ! false      |
    PageRequest(9, 2)    ! List()                             ! true       ! false      |
    PageRequest(0, 0)    ! List()                             ! false      ! true       |
    PageRequest(1, 1)    ! List("a2")                         ! true       ! true       |
    PageRequest(1, 2)    ! List("a3", "a4")                   ! true       ! true       |> {
      (pageRequest, expected, prevPage, nextPage) =>
        val pageResult = queryRepository.retrieveBy(queueName, status, pageRequest)
        pageResult.jobs.map(_.id) must beEqualTo(expected.map(AggregateId))
        pageResult.previousExists must beEqualTo(prevPage)
        pageResult.nextExists must beEqualTo(nextPage)
    }
  }

  "retrieve jobs ready to trigger" in new context {
    val triggeredJobCreatedEvent = JobCreated(aggregateRootId, Version.init, queueName, created, DateTime.now().minusHours(1), payload)
    val untriggeredJobCreatedEvent = JobCreated(aggregateRootId, Version.init, queueName, created, DateTime.now().plusHours(1), payload)

    queryRepository.retrieveWithPendingTrigger must beEmpty
    queryRepositoryUpdater.handle(EventRecord(EventSequenceId(1), untriggeredJobCreatedEvent))
    queryRepository.retrieveWithPendingTrigger must beEmpty
    queryRepositoryUpdater.handle(EventRecord(EventSequenceId(2), triggeredJobCreatedEvent))
    queryRepository.retrieveWithPendingTrigger.size must beEqualTo(1)
  }

  private def createJobEvent(aggregateId: AggregateId, eventId: EventSequenceId, queryRepositoryUpdater: RedisQueryStoreUpdater) = {
    val event = JobCreated(aggregateId, Version.init, queueName, created, triggerDate, payload)
    queryRepositoryUpdater.handle(EventRecord(eventId, event))
    event
  }
}
