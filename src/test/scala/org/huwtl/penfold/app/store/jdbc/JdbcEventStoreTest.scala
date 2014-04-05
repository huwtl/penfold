package org.huwtl.penfold.app.store.jdbc

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables
import org.huwtl.penfold.app.support.json.EventSerializer
import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.event.{Event, JobTriggered, JobCreated}
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.Binding
import org.huwtl.penfold.domain.model.BoundQueue
import org.huwtl.penfold.domain.exceptions.AggregateConflictException
import org.huwtl.penfold.support.JdbcSpecification
import org.specs2.specification.Scope

class JdbcEventStoreTest extends Specification with DataTables with JdbcSpecification {

  class context extends Scope {
    val store = new JdbcEventStore(newDatabase(), new EventSerializer)
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
        store.add(event)
        store.retrieveBy(event.aggregateId) must beEqualTo(expected)
    }
  }

  "prevent concurrent modifications to aggregate" in new context {
    val event = createdEvent(AggregateId("a1"), AggregateVersion(1))
    store.add(event)
    store.add(event) must throwA[AggregateConflictException]
  }

  private def createdEvent(aggregateId: AggregateId, aggregateVersion: AggregateVersion): Event = {
    JobCreated(aggregateId, aggregateVersion, new DateTime(2014, 4, 3, 12, 0, 0, 0), Binding(List(BoundQueue(QueueId("q1")))), new DateTime(2014, 4, 3, 13, 0, 0, 0), Payload.empty)
  }

  private def triggeredEvent(aggregateId: AggregateId, aggregateVersion: AggregateVersion): Event = {
    JobTriggered(aggregateId, aggregateVersion, new DateTime(2014, 4, 3, 12, 0, 0, 0), List(QueueId("q2")))
  }
}
