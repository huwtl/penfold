package org.huwtl.penfold.query

import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.specs2.specification.Scope
import java.util.UUID
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.support.RedisSpecification
import org.specs2.matcher.DataTables
import org.huwtl.penfold.domain.model.QueueName
import org.huwtl.penfold.domain.model.Id
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.domain.event.JobCreated

class RedisQueryRepositoryTest extends RedisSpecification with DataTables {
  val aggregateRootId = Id(UUID.randomUUID().toString)

  val queueName = QueueName("type")

  val payload = Payload(Map("a" -> "123", "b" -> 1))

  val created = new DateTime(2014, 2, 22, 12, 0, 0, 0)

  val triggerDate = new DateTime(2014, 2, 22, 12, 30, 0, 0)

  val status = Status.Waiting

  class context extends Scope {
    val redisClient = newRedisClient()
    val queryRepositoryUpdater = new RedisQueryStoreEventPersister(redisClient, new ObjectSerializer)
    val queryRepository = new RedisQueryRepository(redisClient, new ObjectSerializer)
  }

  "retrieve job by id" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, Version.init, queueName, created, triggerDate, payload)

    queryRepository.retrieveBy(aggregateRootId) must beNone
    queryRepositoryUpdater.handle(NewEvent(Id("1"), jobCreatedEvent))
    queryRepository.retrieveBy(aggregateRootId) must not(beNone)
  }


  "retrieve jobs by status" in new context {
    val jobCreatedEvent = JobCreated(aggregateRootId, Version.init, queueName, created, triggerDate, payload)

    queryRepository.retrieveBy(queueName, status, PageRequest(0, 10)).jobs must beEmpty
    queryRepositoryUpdater.handle(NewEvent(Id("1"), jobCreatedEvent))
    queryRepository.retrieveBy(queueName, status, PageRequest(0, 10)).jobs.size must beEqualTo(1)
  }

  "retrieve jobs by page" in new context {
    createJobEvent(Id("a1"), Id("1"), queryRepositoryUpdater)
    createJobEvent(Id("a2"), Id("2"), queryRepositoryUpdater)
    createJobEvent(Id("a3"), Id("3"), queryRepositoryUpdater)
    createJobEvent(Id("a4"), Id("4"), queryRepositoryUpdater)
    createJobEvent(Id("a5"), Id("5"), queryRepositoryUpdater)

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
        pageResult.jobs.map(_.id) must beEqualTo(expected.map(Id))
        pageResult.previousExists must beEqualTo(prevPage)
        pageResult.nextExists must beEqualTo(nextPage)
    }
  }

  "retrieve jobs ready to trigger" in new context {
    val triggeredJobCreatedEvent = JobCreated(aggregateRootId, Version.init, queueName, created, DateTime.now().minusHours(1), payload)
    val untriggeredJobCreatedEvent = JobCreated(aggregateRootId, Version.init, queueName, created, DateTime.now().plusHours(1), payload)

    queryRepository.retrieveWithPendingTrigger must beEmpty
    queryRepositoryUpdater.handle(NewEvent(Id("1"), untriggeredJobCreatedEvent))
    queryRepository.retrieveWithPendingTrigger must beEmpty
    queryRepositoryUpdater.handle(NewEvent(Id("2"), triggeredJobCreatedEvent))
    queryRepository.retrieveWithPendingTrigger.size must beEqualTo(1)
  }

  private def createJobEvent(aggregateId: Id, eventId: Id, queryRepositoryUpdater: RedisQueryStoreEventPersister) = {
    val event = JobCreated(aggregateId, Version.init, queueName, created, triggerDate, payload)
    queryRepositoryUpdater.handle(NewEvent(eventId, event))
    event
  }
}
