package org.huwtl.penfold.app.store.postgres

import org.specs2.mutable.Specification
import org.huwtl.penfold.support.PostgresSpecification
import org.specs2.specification.Scope
import org.huwtl.penfold.app.support.json.EventSerializer
import org.huwtl.penfold.domain.event.TaskTriggered
import org.huwtl.penfold.domain.model.{AggregateVersion, AggregateId}
import org.huwtl.penfold.readstore.{EventRecord, EventSequenceId}
import org.joda.time.DateTime
import org.specs2.mock.Mockito

class PostgresDomainEventQueryServiceTest extends Specification with PostgresSpecification with Mockito {

  class context extends Scope {
    val created = new DateTime(2014, 3, 1, 12, 0, 0, 0)
    val database = newDatabase()
    val store = new PostgresEventStore(database, new EventSerializer)
    val queryService = new PostgresDomainEventQueryService(database, new EventSerializer, eventRetrievalRetries = 2)
    val event1 = TaskTriggered(AggregateId("a1"), AggregateVersion.init, created)
    val event2 = TaskTriggered(AggregateId("a2"), AggregateVersion.init, created)
  }

  "retrieve nothing for last id when domain event store is empty" in new context {
    queryService.retrieveIdOfLast must beNone
  }

  "retrieve id of last event added to domain event store" in new context {
    store.add(event1)
    store.add(event2)

    queryService.retrieveIdOfLast must beEqualTo(Some(EventSequenceId(1)))
  }

  "retrieve id of last event added to domain event store" in new context {
    store.add(event1)
    store.add(event2)

    queryService.retrieveIdOfLast must beEqualTo(Some(EventSequenceId(1)))
  }

  "retrieve event from domain event store" in new context {
    store.add(event1)
    store.add(event2)

    queryService.retrieveBy(EventSequenceId(0)) must beEqualTo(Some(new EventRecord(EventSequenceId(0), event1)))
    queryService.retrieveBy(EventSequenceId(2)) must beNone
  }
}
