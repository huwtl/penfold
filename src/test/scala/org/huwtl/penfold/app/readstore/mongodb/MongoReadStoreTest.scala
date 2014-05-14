package org.huwtl.penfold.app.readstore.mongodb

import org.specs2.mutable._
import com.github.athieriot.EmbedConnection
import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.specs2.specification.Scope
import org.huwtl.penfold.readstore._
import com.mongodb.casbah.Imports._
import org.huwtl.penfold.readstore.EventRecord
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.event.{TaskCreated, TaskCreatedEvent, FutureTaskCreated, Event}
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.readstore.PageRequest
import org.huwtl.penfold.domain.model.QueueBinding
import org.specs2.matcher.DataTables
import org.huwtl.penfold.app.support.json.ObjectSerializer
import scala.util.Random
import org.huwtl.penfold.domain.model.Status.{Ready, Waiting}
import org.specs2.mock.Mockito
import org.huwtl.penfold.app.support.DateTimeSource

class MongoReadStoreTest extends Specification with DataTables with Mockito with EmbedConnection {
  sequential

  class context extends Scope {
    val queueId = QueueId("q1")
    val payload = Payload(Map("a" -> "123", "b" -> "1"))
    val none : Option[String] = None
    val created = new DateTime(2014, 2, 22, 12, 0, 0, 0)
    val triggerDate = new DateTime(2014, 2, 22, 12, 30, 0, 0)
    val score = triggerDate.getMillis
    val indexes = Indexes(List(Index(List(IndexField("a", "payload.a"))), Index(List(IndexField("a", "payload.a"), IndexField("b", "payload.b", multiKey = true)))))
    val mongoClient = MongoClient("localhost", embedConnectionPort())
    val database = mongoClient("penfoldtest")
    val dateTimeSource = mock[DateTimeSource]
    val readStoreUpdater = new MongoReadStoreUpdater(database, new MongoEventTracker("tracker", database), new ObjectSerializer)
    val readStore = new MongoReadStore(database, indexes, new ObjectSerializer, dateTimeSource)

    def persist(events: List[Event]) = {
      Random.shuffle(events).zipWithIndex.foreach{
        case (event, index) => readStoreUpdater.handle(EventRecord(EventSequenceId(index + 1), event))
      }
    }

    def entry(aggregateId: String, triggerDate: DateTime) = {
      FutureTaskCreated(AggregateId(aggregateId), AggregateVersion.init, created, QueueBinding(queueId), triggerDate, payload, triggerDate.getMillis)
    }

    def forwardFrom(lastEvent: TaskCreatedEvent) = Some(PageReference(s"${lastEvent.aggregateId.value}~${lastEvent.triggerDate.getMillis}~1"))

    def backFrom(lastEvent: TaskCreatedEvent) = Some(PageReference(s"${lastEvent.aggregateId.value}~${lastEvent.triggerDate.getMillis}~0"))

    def setupEntries() = {
      val entries = List(
        entry("f", triggerDate.plusDays(2)),
        entry("e", triggerDate.plusDays(1)),
        entry("d", triggerDate.minusDays(0)),
        entry("c", triggerDate.minusDays(1)),
        entry("b", triggerDate.minusDays(2)),
        entry("a", triggerDate.minusDays(3))
      )
      persist(entries)
      entries
    }
  }

  "check connectivity" in new context {
    readStore.checkConnectivity.isLeft must beTrue
    mongoClient.close()
    readStore.checkConnectivity.isRight must beTrue
  }

  "retrieve waiting tasks to trigger" in new context {
    dateTimeSource.now returns triggerDate
    setupEntries()

    readStore.retrieveTasksToTrigger.toList.map(_.id.value) must beEqualTo(List("a", "b", "c", "d"))
  }

  "retrieve task by id" in new context {
    setupEntries()

    readStore.retrieveBy(AggregateId("a")).isDefined must beTrue
    readStore.retrieveBy(AggregateId("unknown")).isDefined must beFalse
  }

  "filtering" should {
    "filter tasks" in new context {
      setupEntries()
      val pageRequest = PageRequest(2)

      readStore.retrieveBy(Filters(List(Filter("a", Some("123")), Filter("b", Some("1")))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f", "e"))
      readStore.retrieveBy(Filters(List(Filter("a", Some("123")))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f", "e"))
      readStore.retrieveBy(Filters(List(Filter("payload.a", Some("123")))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f", "e"))
      readStore.retrieveBy(Filters(List(Filter("unknown", None))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f", "e"))
      readStore.retrieveBy(Filters(List(Filter("unknown", Some("123")))), pageRequest).entries.map(_.id.value) must beEqualTo(Nil)
      readStore.retrieveBy(Filters(List(Filter("a", Some("mismatch")), Filter("b", Some("1")))), pageRequest).entries.map(_.id.value) must beEmpty
      readStore.retrieveBy(Filters(List(Filter("a", Some("123")), Filter("b", Some("mismatch")))), pageRequest).entries.map(_.id.value) must beEmpty
    }

    "apply or operator for multi value filters" in new context {
      val event1 = TaskCreated(AggregateId("1"), AggregateVersion.init, created, QueueBinding(queueId), triggerDate, Payload(Map("a" -> "ABC")), score)
      val event2 = TaskCreated(AggregateId("2"), AggregateVersion.init, created, QueueBinding(queueId), triggerDate, Payload(Map("a" -> "ABC")), score)
      val event3 = TaskCreated(AggregateId("3"), AggregateVersion.init, created, QueueBinding(queueId), triggerDate, Payload(Map("a" -> "DEF")), score)
      val event4 = TaskCreated(AggregateId("4"), AggregateVersion.init, created, QueueBinding(queueId), triggerDate, Payload(Map("a" -> "")), score)
      val event5 = TaskCreated(AggregateId("5"), AggregateVersion.init, created, QueueBinding(queueId), triggerDate, Payload.empty, score)
      persist(List(event1, event2, event3, event4, event5))

      "page"            | "filter"                                       | "expected"          |
        PageRequest(5)  ! Filter("a", Set(Option("ABC"), Option("DEF"))) ! List("3", "2", "1") |
        PageRequest(5)  ! Filter("a", Set(Option("DEF")))                ! List("3")           |
        PageRequest(5)  ! Filter("a", Set(Option("DEF"), none))          ! List("5", "3")      |
        PageRequest(5)  ! Filter("a", Set(none))                         ! List("5")           |
        PageRequest(5)  ! Filter("a", Set(Option("")))                   ! List("4")           |
        PageRequest(5)  ! Filter("a", Set(Option("ABC")))                ! List("2", "1")      |> {(page, filter, expected) =>

        val pageResult = readStore.retrieveByQueue(queueId, Ready, page, Filters(List(filter)))
        pageResult.entries.map(_.id) must beEqualTo(expected.map(AggregateId))
      }
    }
  }

  "pagination" should {
    "retrieve tasks by next page" in new context {
      val entries = setupEntries()

      "page"                                  | "expected"                         | "hasPrev" | "hasNext" |
      PageRequest(10)                         ! List("f", "e", "d", "c", "b", "a") ! false     ! false     |
      PageRequest(6)                          ! List("f", "e", "d", "c", "b", "a") ! false     ! false     |
      PageRequest(5)                          ! List("f", "e", "d", "c", "b"     ) ! false     ! true      |
      PageRequest(1)                          ! List("f")                          ! false     ! true      |
      PageRequest(0)                          ! Nil                                ! false     ! false     |
      PageRequest(0, forwardFrom(entries(0))) ! Nil                                ! false     ! false     |
      PageRequest(2, forwardFrom(entries(0))) ! List("e", "d")                     ! true      ! true      |
      PageRequest(2, forwardFrom(entries(2))) ! List("c", "b")                     ! true      ! true      |
      PageRequest(2, forwardFrom(entries(1))) ! List("d", "c")                     ! true      ! true      |
      PageRequest(2, forwardFrom(entries(5))) ! Nil                                ! false     ! false     |
      PageRequest(2, forwardFrom(entries(4))) ! List("a")                          ! true      ! false     |
      PageRequest(2, forwardFrom(entries(3))) ! List("b", "a")                     ! true      ! false     |> {(page, expected, hasPrev, hasNext) =>

        val pageResult = readStore.retrieveByQueue(queueId, Waiting, page)
        pageResult.entries.map(_.id) must beEqualTo(expected.map(AggregateId))
        pageResult.previousPage.isDefined must beEqualTo(hasPrev)
        pageResult.nextPage.isDefined must beEqualTo(hasNext)
      }
    }

    "retrieve tasks by previous page" in new context {
      val entries = setupEntries()

      "page"                                 | "expected"                         | "hasPrev" | "hasNext" |
        PageRequest(2, backFrom(entries(5))) ! List("c", "b")                     ! true      ! true      |
        PageRequest(0, backFrom(entries(5))) ! Nil                                ! false     ! false     |
        PageRequest(2, backFrom(entries(2))) ! List("f", "e")                     ! false     ! true      |
        PageRequest(2, backFrom(entries(3))) ! List("e", "d")                     ! true      ! true      |
        PageRequest(2, backFrom(entries(0))) ! Nil                                ! false     ! false     |
        PageRequest(2, backFrom(entries(3))) ! List("e", "d")                     ! true      ! true      |> {(page, expected, hasPrev, hasNext) =>

        val pageResult = readStore.retrieveByQueue(queueId, Waiting, page)
        pageResult.entries.map(_.id) must beEqualTo(expected.map(AggregateId))
        pageResult.previousPage.isDefined must beEqualTo(hasPrev)
        pageResult.nextPage.isDefined must beEqualTo(hasNext)
      }
    }

    "retrieve tasks by next page with additional filter" in new context {
      val event1 = TaskCreated(AggregateId("1"), AggregateVersion.init, created, QueueBinding(queueId), triggerDate, Payload(Map("a" -> "ABC")), score)
      val event2 = TaskCreated(AggregateId("2"), AggregateVersion.init, created, QueueBinding(queueId), triggerDate, Payload(Map("a" -> "ABC")), score)
      val event3 = TaskCreated(AggregateId("3"), AggregateVersion.init, created, QueueBinding(queueId), triggerDate, Payload(Map("a" -> "DEF")), score)
      persist(List(event1, event2, event3))

      "page"            | "expected"       | "hasPrev" | "hasNext" |
        PageRequest(2)  ! List("2", "1")   ! false     ! false     |
        PageRequest(1)  ! List("2")        ! false     ! true      |> {(page, expected, hasPrev, hasNext) =>

        val pageResult = readStore.retrieveByQueue(queueId, Ready, page, Filters(List(Filter("a", Some("ABC")))))
        pageResult.entries.map(_.id) must beEqualTo(expected.map(AggregateId))
        pageResult.previousExists must beEqualTo(hasPrev)
        pageResult.nextExists must beEqualTo(hasNext)
      }
    }
  }
}
