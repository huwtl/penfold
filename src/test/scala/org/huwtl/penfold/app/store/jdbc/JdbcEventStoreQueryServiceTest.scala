package org.huwtl.penfold.app.store.jdbc

import org.specs2.mutable.Specification
import org.huwtl.penfold.support.JdbcSpecification
import org.specs2.specification.Scope
import org.huwtl.penfold.app.support.json.EventSerializer
import org.huwtl.penfold.domain.event.JobTriggered
import org.huwtl.penfold.domain.model.{AggregateVersion, AggregateId}
import org.huwtl.penfold.query.{EventRecord, EventSequenceId}
import org.joda.time.DateTime

class JdbcEventStoreQueryServiceTest extends Specification with JdbcSpecification {

  class context extends Scope {
    val created = new DateTime(2014, 3, 1, 12, 0, 0, 0)
    val database = newDatabase()
    val store = new JdbcEventStore(database, new EventSerializer)
    val queryService = new JdbcEventStoreQueryService(database, new EventSerializer)
  }

  "retrieve nothing for last id when domain event store is empty" in new context {
    queryService.retrieveIdOfLast must beNone
  }

  "retrieve id of last event added to domain event store" in new context {
    val event1 = JobTriggered(AggregateId("a1"), AggregateVersion.init, created, List())
    val event2 = JobTriggered(AggregateId("a2"), AggregateVersion.init, created, List())
    store.add(event1)
    store.add(event2)

    queryService.retrieveIdOfLast must beEqualTo(Some(EventSequenceId(1)))
  }

  "retrieve id of last event added to domain event store" in new context {
    val event1 = JobTriggered(AggregateId("a1"), AggregateVersion.init, created, List())
    val event2 = JobTriggered(AggregateId("a2"), AggregateVersion.init, created, List())
    store.add(event1)
    store.add(event2)

    queryService.retrieveIdOfLast must beEqualTo(Some(EventSequenceId(1)))
  }

  "retrieve event from domain event store" in new context {
    val event1 = JobTriggered(AggregateId("a1"), AggregateVersion.init, created, List())
    val event2 = JobTriggered(AggregateId("a2"), AggregateVersion.init, created, List())
    store.add(event1)
    store.add(event2)

    queryService.retrieveBy(EventSequenceId(0)) must beEqualTo(Some(new EventRecord(EventSequenceId(0), event1)))
    queryService.retrieveBy(EventSequenceId(2)) must beNone
  }
}
