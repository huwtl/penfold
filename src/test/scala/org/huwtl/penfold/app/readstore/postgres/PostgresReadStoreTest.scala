package org.huwtl.penfold.app.readstore.postgres

import java.util.concurrent.TimeUnit

import org.huwtl.penfold.app.support.DateTimeSource
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.event.{FutureTaskCreated, _}
import org.huwtl.penfold.domain.model.{AggregateId, QueueBinding, QueueId, _}
import org.huwtl.penfold.readstore.{EQ, PageReference, _}
import org.huwtl.penfold.support.PostgresSpecification
import org.joda.time.DateTime
import org.specs2.mock.Mockito
import org.specs2.specification.Scope

import scala.collection.mutable
import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import org.huwtl.penfold.domain.model.Status.{Ready, Waiting}
import org.huwtl.penfold.readstore.QueryParamType.NumericType
import org.specs2.matcher.DataTables
import scala.concurrent.duration.FiniteDuration
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{StaticQuery => Q}
import scala.util.Random

class PostgresReadStoreTest extends PostgresSpecification with Mockito with DataTables {
  sequential

  val database = newDatabase()

  def clearDownExistingDatabase() = {
    database.withDynTransaction {
      sqlu"""DELETE FROM tasks""".execute
      sqlu"""DELETE FROM archived""".execute
    }
  }

  class context extends Scope {
    clearDownExistingDatabase()
    val queueId = QueueId("q1")
    val none: Option[String] = None
    val created = new DateTime(2014, 2, 22, 12, 0, 0, 0)
    val triggerDate = new DateTime(2014, 2, 22, 12, 30, 0, 0)
    val score = triggerDate.getMillis
    val dateTimeSource = mock[DateTimeSource]
    val aliases = Aliases(Map(Alias("a") -> Path("payload.a"), Alias("b") -> Path("payload.b"), Alias("c") -> Path("payload.c")))

    val readStoreUpdater = new PostgresReadStoreUpdater(database, new ObjectSerializer)
    val readStore = new PostgresReadStore(database, new PaginatedQueryService(database, new ObjectSerializer, aliases), new ObjectSerializer, dateTimeSource, aliases)

    def persist(events: List[Event]) = {
      database.withDynTransaction {
        Random.shuffle(events).zipWithIndex.foreach {
          case (event, index) => readStoreUpdater.handle(event)
        }
      }
    }

    def entry(aggregateId: String, triggerDate: DateTime, index: Int) = {
      val payload = Payload(Map("a" -> "123", "b" -> "1", "c" -> index))
      FutureTaskCreated(AggregateId(aggregateId), AggregateVersion.init, created.plusSeconds(1), QueueBinding(queueId), triggerDate, payload, triggerDate.getMillis)
    }

    def forwardFrom(lastEvent: TaskCreatedEvent) = Some(PageReference(s"${lastEvent.aggregateId.value}~${lastEvent.triggerDate.getMillis}~1"))

    def backFrom(lastEvent: TaskCreatedEvent) = Some(PageReference(s"${lastEvent.aggregateId.value}~${lastEvent.triggerDate.getMillis}~0"))

    def setupEntries() = {
      val entries = List(
        entry("f", triggerDate.plusDays(2), 1),
        entry("e", triggerDate.plusDays(1), 2),
        entry("d", triggerDate.minusDays(0), 3),
        entry("c", triggerDate.minusDays(1), 4),
        entry("b", triggerDate.minusDays(2), 5),
        entry("a", triggerDate.minusDays(3), 6)
      )
      persist(entries)
      entries
    }
  }

  "check connectivity" in new context {
    readStore.checkConnectivity.isLeft must beTrue
  }

  "retrieve task by id" in new context {
    setupEntries()

    readStore.retrieveBy(AggregateId("a")).isDefined must beTrue
    readStore.retrieveBy(AggregateId("unknown")).isDefined must beFalse
  }

  "retrieve waiting tasks to trigger" in new context {
    dateTimeSource.now returns triggerDate
    setupEntries()

    val triggeredTasks = new mutable.ListBuffer[String]()
    readStore.forEachTriggeredTask(task => {
      triggeredTasks += task.id.value
    })

    triggeredTasks must beEqualTo(mutable.ListBuffer("a", "b", "c", "d"))
  }

  "retrieve tasks to timeout" in new context {
    dateTimeSource.now returns created
    val event1 = TaskCreated(AggregateId("1"), AggregateVersion.init, created.minusSeconds(1), QueueBinding(queueId), created.minusSeconds(1), Payload(Map()), score)
    val event2 = TaskCreated(AggregateId("2"), AggregateVersion.init, created.minusSeconds(3), QueueBinding(queueId), created.minusSeconds(3), Payload(Map()), score)
    val event3 = TaskCreated(AggregateId("3"), AggregateVersion.init, created.minusSeconds(4), QueueBinding(queueId), created.minusSeconds(4), Payload(Map()), score)
    val event4 = TaskCreated(AggregateId("4"), AggregateVersion.init, created.minusSeconds(2), QueueBinding(queueId), created.minusSeconds(2), Payload(Map()), score)
    persist(List(event1, event2, event3, event4))

    val timedOutTasks = new mutable.ListBuffer[String]()
    readStore.forEachTimedOutTask(Ready, FiniteDuration(3l, TimeUnit.SECONDS), task => {
      timedOutTasks += task.id.value
    })

    timedOutTasks must beEqualTo(mutable.ListBuffer("3", "2"))
  }

  "filtering" should {
    "filter tasks on equality" in new context {
      setupEntries()
      val pageRequest = PageRequest(2)

      readStore.retrieveBy(Filters(List(EQ("payload.a", "123"))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f", "e"))
      readStore.retrieveBy(Filters(List(EQ("a", "123"))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f", "e"))
      readStore.retrieveBy(Filters(List(EQ("a", "123"), EQ("payload.b", "1"))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f", "e"))
      readStore.retrieveBy(Filters(List(EQ("payload.unknown", null))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f", "e"))
      readStore.retrieveBy(Filters(List(EQ("payload.unknown", "123"))), pageRequest).entries.map(_.id.value) must beEqualTo(Nil)
      readStore.retrieveBy(Filters(List(EQ("a", "mismatch"), EQ("payload.b", "1"))), pageRequest).entries.map(_.id.value) must beEmpty
      readStore.retrieveBy(Filters(List(EQ("a", "123"), EQ("payload.b", "mismatch"))), pageRequest).entries.map(_.id.value) must beEmpty
    }

    "filter tasks with less than comparison" in new context {
      setupEntries()
      val pageRequest = PageRequest(2)

      readStore.retrieveBy(Filters(List(LT("payload.c", "3", NumericType))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f", "e"))
      readStore.retrieveBy(Filters(List(LT("c", "100", NumericType))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f", "e"))
      readStore.retrieveBy(Filters(List(LT("c", "2", NumericType))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f"))
      readStore.retrieveBy(Filters(List(LT("c", "2", NumericType), EQ("payload.b", "1"))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f"))
      readStore.retrieveBy(Filters(List(LT("c", "2", NumericType), EQ("payload.b", "2"))), pageRequest).entries.map(_.id.value) must beEqualTo(Nil)
      readStore.retrieveBy(Filters(List(LT("c", "1", NumericType))), pageRequest).entries.map(_.id.value) must beEqualTo(Nil)
      readStore.retrieveBy(Filters(List(LT("c", "-1", NumericType))), pageRequest).entries.map(_.id.value) must beEqualTo(Nil)
      readStore.retrieveBy(Filters(List(LT("c", null, NumericType))), pageRequest).entries.map(_.id.value) must beEqualTo(Nil)
    }

    "filter tasks with greater than comparison" in new context {
      setupEntries()
      val pageRequest = PageRequest(2)

      readStore.retrieveBy(Filters(List(GT("payload.c", "3", NumericType))), pageRequest).entries.map(_.id.value) must beEqualTo(List("c", "b"))
      readStore.retrieveBy(Filters(List(GT("c", "0", NumericType))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f", "e"))
      readStore.retrieveBy(Filters(List(GT("c", "-1", NumericType))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f", "e"))
      readStore.retrieveBy(Filters(List(GT("c", "2", NumericType))), pageRequest).entries.map(_.id.value) must beEqualTo(List("d", "c"))
      readStore.retrieveBy(Filters(List(GT("c", "5", NumericType), EQ("b", "1"))), pageRequest).entries.map(_.id.value) must beEqualTo(List("a"))
      readStore.retrieveBy(Filters(List(GT("c", "5", NumericType), EQ("b", "2"))), pageRequest).entries.map(_.id.value) must beEqualTo(Nil)
      readStore.retrieveBy(Filters(List(GT("c", "6", NumericType))), pageRequest).entries.map(_.id.value) must beEqualTo(Nil)
      readStore.retrieveBy(Filters(List(GT("c", null, NumericType))), pageRequest).entries.map(_.id.value) must beEqualTo(Nil)
    }

    "apply or operator for multi value filters" in new context {
      val event1 = TaskCreated(AggregateId("1"), AggregateVersion.init, created, QueueBinding(queueId), triggerDate, Payload(Map("a" -> "ABC")), score)
      val event2 = TaskCreated(AggregateId("2"), AggregateVersion.init, created, QueueBinding(queueId), triggerDate, Payload(Map("a" -> "ABC")), score)
      val event3 = TaskCreated(AggregateId("3"), AggregateVersion.init, created, QueueBinding(queueId), triggerDate, Payload(Map("a" -> "DEF")), score)
      val event4 = TaskCreated(AggregateId("4"), AggregateVersion.init, created, QueueBinding(queueId), triggerDate, Payload(Map("a" -> "")), score)
      val event5 = TaskCreated(AggregateId("5"), AggregateVersion.init, created, QueueBinding(queueId), triggerDate, Payload.empty, score)
      persist(List(event1, event2, event3, event4, event5))

      "page"            | "filter"                           | "expected"          |
        PageRequest(5)  ! IN("payload.a", Set("ABC", "DEF")) ! List("3", "2", "1") |
        PageRequest(5)  ! IN("a", Set("DEF"))                ! List("3")           |
        PageRequest(5)  ! IN("a", Set("DEF", null))          ! List("3")           |
        PageRequest(5)  ! IN("a", Set(null))                 ! List()              |
        PageRequest(5)  ! IN("a", Set(""))                   ! List("4")           |
        PageRequest(5)  ! IN("a", Set("ABC"))                ! List("2", "1")      |> {(page, filter, expected) =>

        val pageResult = readStore.retrieveByQueue(queueId, Ready, page, SortOrder.Desc, Filters(List(filter)))
        pageResult.entries.map(_.id) must beEqualTo(expected.map(AggregateId))
      }
    }
  }

  "pagination" should {
    "retrieve tasks by next page in descending order" in new context {
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

        val pageResult = readStore.retrieveByQueue(queueId, Waiting, page, SortOrder.Desc)
        pageResult.entries.map(_.id) must beEqualTo(expected.map(AggregateId))
        pageResult.previousPage.isDefined must beEqualTo(hasPrev)
        pageResult.nextPage.isDefined must beEqualTo(hasNext)
      }
    }

    "retrieve tasks by next page in ascending order" in new context {
      val entries = setupEntries()

      "page"                                    | "expected"                         | "hasPrev" | "hasNext" |
        PageRequest(10)                         ! List("a", "b", "c", "d", "e", "f") ! false     ! false     |
        PageRequest(6)                          ! List("a", "b", "c", "d", "e", "f") ! false     ! false     |
        PageRequest(5)                          ! List("a", "b", "c", "d", "e")      ! false     ! true      |
        PageRequest(1)                          ! List("a")                          ! false     ! true      |
        PageRequest(0)                          ! Nil                                ! false     ! false     |
        PageRequest(0, forwardFrom(entries(5))) ! Nil                                ! false     ! false     |
        PageRequest(2, forwardFrom(entries(5))) ! List("b", "c")                     ! true      ! true      |
        PageRequest(2, forwardFrom(entries(3))) ! List("d", "e")                     ! true      ! true      |
        PageRequest(2, forwardFrom(entries(4))) ! List("c", "d")                     ! true      ! true      |
        PageRequest(2, forwardFrom(entries(0))) ! Nil                                ! false     ! false     |
        PageRequest(2, forwardFrom(entries(1))) ! List("f")                          ! true      ! false     |
        PageRequest(2, forwardFrom(entries(2))) ! List("e", "f")                     ! true      ! false     |> {(page, expected, hasPrev, hasNext) =>

        val pageResult = readStore.retrieveByQueue(queueId, Waiting, page, SortOrder.Asc)
        pageResult.entries.map(_.id) must beEqualTo(expected.map(AggregateId))
        pageResult.previousPage.isDefined must beEqualTo(hasPrev)
        pageResult.nextPage.isDefined must beEqualTo(hasNext)
      }
    }

    "retrieve tasks by previous page in descending order" in new context {
      val entries = setupEntries()

      "page"                                 | "expected"                         | "hasPrev" | "hasNext" |
        PageRequest(2, backFrom(entries(5))) ! List("c", "b")                     ! true      ! true      |
        PageRequest(0, backFrom(entries(5))) ! Nil                                ! false     ! false     |
        PageRequest(2, backFrom(entries(2))) ! List("f", "e")                     ! false     ! true      |
        PageRequest(2, backFrom(entries(0))) ! Nil                                ! false     ! false     |
        PageRequest(2, backFrom(entries(3))) ! List("e", "d")                     ! true      ! true      |> {(page, expected, hasPrev, hasNext) =>

        val pageResult = readStore.retrieveByQueue(queueId, Waiting, page, SortOrder.Desc)
        pageResult.entries.map(_.id) must beEqualTo(expected.map(AggregateId))
        pageResult.previousPage.isDefined must beEqualTo(hasPrev)
        pageResult.nextPage.isDefined must beEqualTo(hasNext)
      }
    }

    "retrieve tasks by previous page in ascending order" in new context {
      val entries = setupEntries()

      "page"                                 | "expected"                         | "hasPrev" | "hasNext" |
        PageRequest(2, backFrom(entries(0))) ! List("d", "e")                     ! true      ! true      |
        PageRequest(0, backFrom(entries(0))) ! Nil                                ! false     ! false     |
        PageRequest(2, backFrom(entries(3))) ! List("a", "b")                     ! false     ! true      |
        PageRequest(2, backFrom(entries(5))) ! Nil                                ! false     ! false     |
        PageRequest(2, backFrom(entries(2))) ! List("b", "c")                     ! true      ! true      |> {(page, expected, hasPrev, hasNext) =>

        val pageResult = readStore.retrieveByQueue(queueId, Waiting, page, SortOrder.Asc)
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

        val pageResult = readStore.retrieveByQueue(queueId, Ready, page, SortOrder.Desc, Filters(List(EQ("a", "ABC"))))
        pageResult.entries.map(_.id) must beEqualTo(expected.map(AggregateId))
        pageResult.previousExists must beEqualTo(hasPrev)
        pageResult.nextExists must beEqualTo(hasNext)
      }
    }
  }

  private def filter(key: String) = EQ(key, null)
}
