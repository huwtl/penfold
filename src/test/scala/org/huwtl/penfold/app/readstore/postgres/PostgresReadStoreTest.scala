package org.huwtl.penfold.app.readstore.postgres

import org.specs2.specification.Scope
import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.huwtl.penfold.app.support.DateTimeSource
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.event._
import scala.util.Random
import org.huwtl.penfold.readstore._
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.event.FutureTaskCreated
import org.huwtl.penfold.domain.model.AggregateId
import scala.Some
import org.huwtl.penfold.readstore.PageReference
import org.huwtl.penfold.readstore.EventRecord
import org.huwtl.penfold.readstore.EQ
import org.huwtl.penfold.domain.model.QueueBinding
import org.specs2.mock.Mockito
import org.huwtl.penfold.support.PostgresSpecification
import scala.collection.mutable
import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.slick.jdbc.{StaticQuery => Q}
import Q.interpolation
import org.huwtl.penfold.readstore.QueryParamType.NumericType
import org.huwtl.penfold.domain.model.Status.Ready
import org.specs2.matcher.DataTables

class PostgresReadStoreTest extends PostgresSpecification with Mockito with DataTables {
  sequential

  val database = newDatabase()

  def clearDownExistingDatabase() = {
    database.withDynSession {
      sqlu"""DELETE FROM tasks""".execute
      sqlu"""DELETE FROM trackers""".execute
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

    val readStoreUpdater = new PostgresReadStoreUpdater(database, new PostgresEventTracker("tracker", database), new ObjectSerializer)
    val readStore = new PostgresReadStore(database, new PaginatedQueryService(database, new ObjectSerializer), new ObjectSerializer, dateTimeSource, new PostgresQueryPlanFactory)

    def persist(events: List[Event]) = {
      Random.shuffle(events).zipWithIndex.foreach {
        case (event, index) => readStoreUpdater.handle(EventRecord(EventSequenceId(index + 1), event))
      }
    }

    def entry(aggregateId: String, triggerDate: DateTime, index: Int) = {
      val payload = Payload(Map("a" -> "123", "b" -> "1", "c" -> index))
      FutureTaskCreated(AggregateId(aggregateId), AggregateVersion.init, created, QueueBinding(queueId), triggerDate, payload, triggerDate.getMillis)
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

  "filtering" should {
    "filter tasks on equality" in new context {
      setupEntries()
      val pageRequest = PageRequest(2)

      readStore.retrieveBy(Filters(List(EQ("a", "123"), EQ("b", "1"))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f", "e"))
      readStore.retrieveBy(Filters(List(EQ("a", "123"))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f", "e"))
      readStore.retrieveBy(Filters(List(EQ("payload.a", "123"))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f", "e"))
      readStore.retrieveBy(Filters(List(EQ("unknown", null))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f", "e"))
      readStore.retrieveBy(Filters(List(EQ("unknown", "123"))), pageRequest).entries.map(_.id.value) must beEqualTo(Nil)
      readStore.retrieveBy(Filters(List(EQ("a", "mismatch"), EQ("b", "1"))), pageRequest).entries.map(_.id.value) must beEmpty
      readStore.retrieveBy(Filters(List(EQ("a", "123"), EQ("b", "mismatch"))), pageRequest).entries.map(_.id.value) must beEmpty
    }

    "filter tasks with less than comparison" in new context {
      setupEntries()
      val pageRequest = PageRequest(2)

      readStore.retrieveBy(Filters(List(LT("c", "3", NumericType))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f", "e"))
      readStore.retrieveBy(Filters(List(LT("c", "100", NumericType))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f", "e"))
      readStore.retrieveBy(Filters(List(LT("c", "2", NumericType))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f"))
      readStore.retrieveBy(Filters(List(LT("c", "2", NumericType), EQ("b", "1"))), pageRequest).entries.map(_.id.value) must beEqualTo(List("f"))
      readStore.retrieveBy(Filters(List(LT("c", "2", NumericType), EQ("b", "2"))), pageRequest).entries.map(_.id.value) must beEqualTo(Nil)
      readStore.retrieveBy(Filters(List(LT("c", "1", NumericType))), pageRequest).entries.map(_.id.value) must beEqualTo(Nil)
      readStore.retrieveBy(Filters(List(LT("c", "-1", NumericType))), pageRequest).entries.map(_.id.value) must beEqualTo(Nil)
      readStore.retrieveBy(Filters(List(LT("c", null, NumericType))), pageRequest).entries.map(_.id.value) must beEqualTo(Nil)
    }

    "filter tasks with greater than comparison" in new context {
      setupEntries()
      val pageRequest = PageRequest(2)

      readStore.retrieveBy(Filters(List(GT("c", "3", NumericType))), pageRequest).entries.map(_.id.value) must beEqualTo(List("c", "b"))
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

      "page"            | "filter"                   | "expected"          |
        PageRequest(5)  ! IN("a", Set("ABC", "DEF")) ! List("3", "2", "1") |
        PageRequest(5)  ! IN("a", Set("DEF"))        ! List("3")           |
        PageRequest(5)  ! IN("a", Set("DEF", null))  ! List("5", "3")      |
        PageRequest(5)  ! IN("a", Set(null))         ! List("5")           |
        PageRequest(5)  ! IN("a", Set(""))           ! List("4")           |
        PageRequest(5)  ! IN("a", Set("ABC"))        ! List("2", "1")      |> {(page, filter, expected) =>

        val pageResult = readStore.retrieveByQueue(queueId, Ready, page, SortOrder.Desc, Filters(List(filter)))
        pageResult.entries.map(_.id) must beEqualTo(expected.map(AggregateId))
      }
    }
  }

  private def filter(key: String) = EQ(key, null)
}
