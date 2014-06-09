package com.qmetric.penfold.app.readstore.mongodb

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables
import com.qmetric.penfold.app.support.json.{ObjectSerializer, EventSerializer}
import com.qmetric.penfold.domain.model._
import com.qmetric.penfold.support.JdbcSpecification
import org.specs2.specification.Scope
import com.qmetric.penfold.app.store.jdbc.{JdbcDomainEventQueryService, JdbcEventStore}
import com.github.athieriot.EmbedConnection
import com.mongodb.casbah.Imports._
import com.qmetric.penfold.domain.model.QueueId
import com.qmetric.penfold.domain.model.AggregateId
import com.qmetric.penfold.domain.model.QueueBinding
import com.qmetric.penfold.app.support.DateTimeSource
import com.qmetric.penfold.domain.store.DomainRepository
import com.qmetric.penfold.readstore.{NewEventsProvider, EventNotifier, EventNotifiers}
import java.util.UUID
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit

class MongoReadStoreUpdaterConcurrencyTest extends Specification with DataTables with JdbcSpecification with EmbedConnection {
  sequential

  class context extends Scope {
    val eventStore = newDatabase()
    val store = new JdbcEventStore(eventStore, new EventSerializer)
    val eventQueryService = new JdbcDomainEventQueryService(eventStore, new EventSerializer)

    val mongoClient = MongoClient("localhost", embedConnectionPort())
    val mongoDatabase = mongoClient("penfoldtest")
    val readStore = new MongoReadStore(mongoDatabase, Indexes(Nil), new ObjectSerializer, new DateTimeSource)
    val readStoreEventProvider = new NewEventsProvider(new MongoNextExpectedEventIdProvider("readStoreEventTracker", mongoDatabase), eventQueryService)
    val readStoreUpdater = new EventNotifier(readStoreEventProvider, new MongoReadStoreUpdater(mongoDatabase, new MongoEventTracker("readStoreEventTracker", mongoDatabase), new ObjectSerializer))

    val eventNotifiers = new EventNotifiers(List(readStoreUpdater))

    val domainRepository = new DomainRepository(store, eventNotifiers)

    def createAndArchiveTasks() = {
      val createdTasks = (1 to 10).map {i =>
          domainRepository.add(Task.create(AggregateId(UUID.randomUUID().toString), QueueBinding(QueueId("q1")), Payload.empty, None)).asInstanceOf[Task]
      }

      createdTasks.foreach {createdTask =>
          domainRepository.add(createdTask.archive)
      }
    }
  }

  "thread test store events" in new context {
    val futures = (1 to 10).map {
      i => future(createAndArchiveTasks())
    }
    futures.foreach(f => Await.result(f, FiniteDuration(20, TimeUnit.MINUTES)))

    val tasksCollection = mongoDatabase("tasks")

    tasksCollection.count() must beEqualTo(0)
  }
}
