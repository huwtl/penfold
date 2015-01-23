package org.huwtl.penfold.app.readstore.postgres

import org.specs2.specification.Scope
import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.huwtl.penfold.app.support.DateTimeSource
import org.huwtl.penfold.app.readstore.mongodb._
import org.huwtl.penfold.app.support.json.ObjectSerializer
import com.mongodb.casbah.Imports._
import org.huwtl.penfold.domain.event._
import scala.util.Random
import org.huwtl.penfold.readstore._
import org.huwtl.penfold.app.readstore.mongodb.Index
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.event.FutureTaskCreated
import org.huwtl.penfold.domain.model.AggregateId
import scala.Some
import org.huwtl.penfold.readstore.PageReference
import org.huwtl.penfold.app.readstore.mongodb.Indexes
import org.huwtl.penfold.app.readstore.mongodb.IndexField
import org.huwtl.penfold.readstore.EventRecord
import org.huwtl.penfold.readstore.EQ
import org.huwtl.penfold.domain.model.QueueBinding
import org.specs2.mock.Mockito
import org.huwtl.penfold.support.PostgresSpecification

class PostgresReadStoreTest extends PostgresSpecification with Mockito {

  sequential

  val database = newDatabase()

  class context extends Scope {
    val queueId = QueueId("q1")
    val none: Option[String] = None
    val created = new DateTime(2014, 2, 22, 12, 0, 0, 0)
    val triggerDate = new DateTime(2014, 2, 22, 12, 30, 0, 0)
    val score = triggerDate.getMillis
    val dateTimeSource = mock[DateTimeSource]
    val indexes = Indexes(List(
      Index(None, List(IndexField("a", "payload.a", multiKey = true))),
      Index(None, List(IndexField("a", "payload.a", multiKey = true), IndexField("b", "payload.b"))),
      Index(None, List(IndexField("c", "payload.c", multiKey = true), IndexField("b", "payload.b")))
    ))


    val readStoreUpdater = new MongoReadStoreUpdater(database, new MongoEventTracker("tracker", database), new ObjectSerializer)
    val taskMapper = new MongoTaskMapper(new ObjectSerializer)
    val readStore = new MongoReadStore(database, indexes, taskMapper, new PaginatedQueryService(database, taskMapper), dateTimeSource)

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

  "retrieve waiting tasks to trigger" in new context {
    dateTimeSource.now returns triggerDate
    setupEntries()

    val tasks = List()
    readStore.forEachTriggeredTask(task => {
      tasks = tasks :: task
    }) beEqualTo(List("a", "b", "c", "d"))
  }

  private def filter(key: String) = EQ(key, null)
}
