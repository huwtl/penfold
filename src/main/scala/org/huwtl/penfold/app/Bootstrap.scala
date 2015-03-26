package org.huwtl.penfold.app

import javax.servlet.ServletContext
import org.scalatra.LifeCycle
import org.huwtl.penfold.app.web._
import java.net.URI
import org.huwtl.penfold.app.support.hal.{HalQueueFormatter, HalTaskFormatter}
import org.huwtl.penfold.command._
import org.huwtl.penfold.domain.store.DomainRepositoryImpl
import org.huwtl.penfold.app.support.json.{ObjectSerializer, EventSerializer}
import org.huwtl.penfold.readstore.EventNotifier
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.FicusConfig._
import org.huwtl.penfold.app.support.{DateTimeSource, UUIDFactory}
import org.huwtl.penfold.app.schedule.{StartedTaskTimeoutScheduler, TaskArchiveScheduler, TaskTriggerScheduler}
import com.codahale.metrics.health.HealthCheckRegistry
import org.huwtl.penfold.app.support.metrics.{ReadStoreConnectivityHealthcheck, EventStoreConnectivityHealthcheck}
import org.huwtl.penfold.app.store.postgres._
import org.huwtl.penfold.app.readstore.postgres.{PaginatedQueryService, PostgresReadStore, PostgresReadStoreUpdater}

class Bootstrap extends LifeCycle {
  override def init(context: ServletContext) {

    val config = ConfigFactory.load().as[ServerConfiguration]("penfold")

    val eventSerializer = new EventSerializer
    val objectSerializer = new ObjectSerializer

    val aggregateIdFactory = new UUIDFactory

    val database = new PostgresDatabaseInitialiser(config.customReadStoreDbMigrationPath).init(new PostgresConnectionPoolFactory().create(config.database))
    val eventStore = new PostgresEventStore(database, eventSerializer)

    val readStoreUpdater = new EventNotifier(new PostgresReadStoreUpdater(database, objectSerializer))

    val domainRepository = new PostgresTransactionalDomainRepository(database, new DomainRepositoryImpl(eventStore, readStoreUpdater))

    val commandDispatcher = new CommandDispatcherFactory(domainRepository, aggregateIdFactory).create

    val readStore = new PostgresReadStore(database, new PaginatedQueryService(database, objectSerializer, config.readStorePathAliases), objectSerializer, new DateTimeSource, config.readStorePathAliases)

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
