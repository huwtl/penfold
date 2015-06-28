package com.qmetric.penfold.app

import javax.servlet.ServletContext
import org.scalatra.LifeCycle
import com.qmetric.penfold.app.web._
import java.net.URI
import com.qmetric.penfold.app.support.hal.{HalQueueFormatter, HalTaskFormatter}
import com.qmetric.penfold.command._
import com.qmetric.penfold.domain.store.DomainRepositoryImpl
import com.qmetric.penfold.app.support.json.{ObjectSerializer, EventSerializer}
import com.qmetric.penfold.readstore.EventNotifier
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import com.qmetric.penfold.app.support.{DateTimeSource, UUIDFactory}
import com.qmetric.penfold.app.schedule.{StartedTaskTimeoutScheduler, TaskArchiveScheduler, TaskTriggerScheduler}
import com.codahale.metrics.health.HealthCheckRegistry
import com.qmetric.penfold.app.support.metrics.{ReadStoreConnectivityHealthcheck, EventStoreConnectivityHealthcheck}
import com.qmetric.penfold.app.store.postgres._
import com.qmetric.penfold.app.readstore.postgres.{PaginatedQueryService, PostgresReadStore, PostgresReadStoreUpdater}

class Bootstrap extends LifeCycle {
  override def init(context: ServletContext) {

    val config = ConfigFactory.load().as[ServerConfiguration]("penfold")

    val eventSerializer = new EventSerializer
    val objectSerializer = new ObjectSerializer

    val aggregateIdFactory = new UUIDFactory

    val database = new PostgresDatabaseInitialiser(config.dbMigrationPath).init(new PostgresConnectionPoolFactory().create(config.database))
    val eventStore = new PostgresEventStore(database, eventSerializer)

    val readStoreUpdater = new EventNotifier(new PostgresReadStoreUpdater(objectSerializer))

    val domainRepository = new PostgresTransactionalDomainRepository(database, new DomainRepositoryImpl(eventStore, readStoreUpdater))

    val commandDispatcher = new CommandDispatcherFactory(domainRepository, aggregateIdFactory).create

    val readStore = new PostgresReadStore(database, new PaginatedQueryService(database, objectSerializer, config.queryPathAliases), objectSerializer, new DateTimeSource, config.queryPathAliases)

    val baseUrl = URI.create(config.publicUrl)

    val baseTaskLink = URI.create(s"${baseUrl.toString}/tasks")

    val baseQueueLink = URI.create(s"${baseUrl.toString}/queues")

    val taskFormatter = new HalTaskFormatter(baseTaskLink, baseQueueLink)

    val queueFormatter = new HalQueueFormatter(baseQueueLink, taskFormatter)

    val healthCheckRegistry: HealthCheckRegistry = new HealthCheckRegistry
    healthCheckRegistry.register("Event store connectivity", new EventStoreConnectivityHealthcheck(eventStore))
    healthCheckRegistry.register("Read store connectivity", new ReadStoreConnectivityHealthcheck(readStore))

    context mount(new PingResource, "/ping")
    context mount(new HealthResource(healthCheckRegistry, objectSerializer), "/healthcheck")
    context mount(new TaskResource(readStore, commandDispatcher, new TaskCommandParser(objectSerializer), taskFormatter, config.pageSize, config.authentication), "/tasks/*")
    context mount(new QueueResource(readStore, queueFormatter, config.sortOrdering.mapping, config.pageSize, config.authentication), "/queues/*")

    new TaskTriggerScheduler(readStore, commandDispatcher, config.triggerCheckFrequency).start()

    if (config.startedTaskTimeout.isDefined) {
      new StartedTaskTimeoutScheduler(readStore, commandDispatcher, config.startedTaskTimeout.get).start()
    }

    if (config.archiver.isDefined) {
      new TaskArchiveScheduler(readStore, commandDispatcher, config.archiver.get).start()
    }
  }
}
