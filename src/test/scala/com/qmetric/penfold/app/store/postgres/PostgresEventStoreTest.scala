package com.qmetric.penfold.app.store.postgres

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables
import com.qmetric.penfold.app.support.json.EventSerializer
import com.qmetric.penfold.domain.model._
import org.joda.time.DateTime
import com.qmetric.penfold.domain.event.{Event, TaskTriggered, TaskCreated}
import com.qmetric.penfold.domain.model.AggregateId
import com.qmetric.penfold.domain.exceptions.AggregateConflictException
import com.qmetric.penfold.support.PostgresSpecification
import org.specs2.specification.Scope
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PostgresEventStoreTest extends Specification with DataTables with PostgresSpecification {

  class context extends Scope {
    val triggerDate = new DateTime(2014, 4, 3, 13, 0, 0, 0)
    val database = newDatabase()
    val store = new PostgresEventStore(database, new EventSerializer)

    def createdEvent(aggregateId: AggregateId, aggregateVersion: AggregateVersion): Event = {
      TaskCreated(aggregateId, aggregateVersion, new DateTime(2014, 4, 3, 12, 0, 0, 0), QueueId("q1"), triggerDate, Payload.empty, triggerDate.getMillis)
    }

    def triggeredEvent(aggregateId: AggregateId, aggregateVersion: AggregateVersion): Event = {
      TaskTriggered(aggregateId, aggregateVersion, new DateTime(2014, 4, 3, 12, 0, 0, 0))
    }
  }

  "store events" in new context {
    val event1 = createdEvent(AggregateId("a1"), AggregateVersion(1))
    val event2 = triggeredEvent(AggregateId("a1"), AggregateVersion(2))
    val event3 = createdEvent(AggregateId("a2"), AggregateVersion(1))

    "event"  || "expected"           |
      event1 !! List(event1)         |
      event2 !! List(event1, event2) |
      event3 !! List(event3)         |> {
      (event, expected) =>
        database.withDynTransaction {
          store.add(event)
          store.retrieveBy(event.aggregateId) must beEqualTo(expected)
        }
    }
  }

  "prevent concurrent modifications to aggregate" in new context {
    val event = createdEvent(AggregateId("a1"), AggregateVersion(1))
    database.withDynTransaction {
      store.add(event)
      store.add(event) must throwA[AggregateConflictException]
    }
  }

  "check connectivity to store" in new context {
    store.checkConnectivity.left.getOrElse(false) must beTrue
  }
}
