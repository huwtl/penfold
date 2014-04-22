package org.huwtl.penfold.app.readstore.mongodb

import com.github.athieriot.EmbedConnection
import org.huwtl.penfold.domain.event.{TaskStarted, TaskCreated}
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.QueueBinding
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.readstore.{TaskRecord, EventSequenceId, EventRecord}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.joda.time.DateTime
import org.huwtl.penfold.app.support.json.{ObjectSerializer, EventSerializer}
import com.mongodb.casbah.Imports._
import org.huwtl.penfold.app.support.DateTimeSource
import java.util.UUID

class MongoReadStoreUpdaterTest extends Specification with EmbedConnection {
  sequential

  trait context extends Scope {
    val aggregateId = AggregateId(UUID.randomUUID().toString)
    val queueId = QueueId("q1")
    val payload = Payload(Map("field1" -> "123", "inner" -> Map("field2" -> 1)))
    val binding = QueueBinding(queueId)
    val created = new DateTime(2014, 2, 22, 12, 0, 0, 0)
    val triggerDate = new DateTime(2014, 2, 22, 12, 30, 0, 0)
    val serializer = new EventSerializer
    val taskCreatedEvent = TaskCreated(aggregateId, AggregateVersion(1), created, QueueBinding(queueId), triggerDate, payload)
    val taskStartedEvent = TaskStarted(aggregateId, AggregateVersion(2), created)

    val mongoClient = MongoClient("localhost", embedConnectionPort())
    val database = mongoClient("penfoldtest")
    val readStore = new MongoReadStore(database, Indexes(Nil), new ObjectSerializer, new DateTimeSource)
    val readStoreUpdater = new MongoReadStoreUpdater(database, new MongoEventTracker("tracking", database), new ObjectSerializer)
  }

  "create task" in new context {
    readStoreUpdater.handle(EventRecord(EventSequenceId(1), taskCreatedEvent))
    readStoreUpdater.handle(EventRecord(EventSequenceId(2), taskStartedEvent))

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TaskRecord(aggregateId, created, binding, Status.Started, taskStartedEvent.created, triggerDate, created.getMillis, payload)))
  }

  "ignore duplicate events" in new context {
    readStoreUpdater.handle(EventRecord(EventSequenceId(1), taskCreatedEvent))
    readStoreUpdater.handle(EventRecord(EventSequenceId(2), taskStartedEvent))
    readStoreUpdater.handle(EventRecord(EventSequenceId(2), taskCreatedEvent))

    val task = readStore.retrieveBy(aggregateId)

    task must beEqualTo(Some(TaskRecord(aggregateId, created, binding, Status.Started, taskStartedEvent.created, triggerDate, created.getMillis, payload)))
  }
}
