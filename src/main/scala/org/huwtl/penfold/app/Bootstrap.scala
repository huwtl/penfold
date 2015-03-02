package org.huwtl.penfold.app

import javax.servlet.ServletContext
import org.scalatra.LifeCycle
import org.huwtl.penfold.app.web._
import java.net.URI
import org.huwtl.penfold.app.support.hal.{HalQueueFormatter, HalTaskFormatter}
import org.huwtl.penfold.command._
import org.huwtl.penfold.domain.store.DomainRepository
import org.huwtl.penfold.app.support.json.{ObjectSerializer, EventSerializer}
import org.huwtl.penfold.readstore.{EventNotifiersImpl, EventNotifier, NewEventsProvider}
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.FicusConfig._
import org.huwtl.penfold.app.support.{DateTimeSource, UUIDFactory}
import org.huwtl.penfold.app.schedule.{ReadyTaskAssignmentTimeoutScheduler, EventSyncScheduler, TaskArchiveScheduler, TaskTriggerScheduler}
import com.codahale.metrics.health.HealthCheckRegistry
import org.huwtl.penfold.app.support.metrics.{ReadStoreConnectivityHealthcheck, EventStoreConnectivityHealthcheck}
import org.huwtl.penfold.app.store.jdbc._
import org.huwtl.penfold.app.readstore.postgres.{PaginatedQueryService, PostgresReadStore, PostgresReadStoreUpdater, PostgresEventTracker}

class Bootstrap extends LifeCycle {
  override def init(context: ServletContext) {

    val config = ConfigFactory.load().as[ServerConfiguration]("penfold")

    val eventSerializer = new EventSerializer
    val objectSerializer = new ObjectSerializer

    val aggregateIdFactory = new UUIDFactory

    val domainJdbcPool = new JdbcDatabaseInitialiser().init(new JdbcConnectionPoolFactory().create(config.domainJdbcConnectionPool))
    val eventStore = new JdbcEventStore(domainJdbcPool, eventSerializer)
    val eventQueryService = new JdbcDomainEventQueryService(domainJdbcPool, eventSerializer)

    val readStoreJdbcPool = new JdbcDatabaseInitialiser().init(new JdbcConnectionPoolFactory().create(config.readStoreJdbcConnectionPool))
    val postgresEventTracker = new PostgresEventTracker("readStoreEventTracker", readStoreJdbcPool)
    val readStoreEventProvider = new NewEventsProvider(postgresEventTracker, eventQueryService)
    val readStoreUpdater = new EventNotifier(readStoreEventProvider, new PostgresReadStoreUpdater(readStoreJdbcPool, postgresEventTracker, objectSerializer))

    val eventNotifiers = new ActorBasedEventNotifiers(new EventNotifiersImpl(List(readStoreUpdater)), noOfWorkers = 3)

    val domainRepository = new DomainRepository(eventStore, eventNotifiers)

    val commandDispatcher = new CommandDispatcherFactory(domainRepository, aggregateIdFactory).create

    val readStore = new PostgresReadStore(readStoreJdbcPool, new PaginatedQueryService(readStoreJdbcPool, objectSerializer, config.readStorePathAliases), objectSerializer, new DateTimeSource, config.readStorePathAliases)

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

    new EventSyncScheduler(eventNotifiers, config.eventSync).start()

    new TaskTriggerScheduler(readStore, commandDispatcher, config.triggeredCheckFrequency).start()

    if (config.readyTaskAssignmentTimeout.isDefined) {
      new ReadyTaskAssignmentTimeoutScheduler(readStore, commandDispatcher, config.readyTaskAssignmentTimeout.get).start()
    }

    if (config.taskArchiver.isDefined) {
      new TaskArchiveScheduler(readStore, commandDispatcher, config.taskArchiver.get).start()
    }
  }
}
