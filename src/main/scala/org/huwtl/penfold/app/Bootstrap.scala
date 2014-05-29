package org.huwtl.penfold.app

import javax.servlet.ServletContext
import org.scalatra.LifeCycle
import org.huwtl.penfold.app.web._
import java.net.URI
import org.huwtl.penfold.app.support.hal.{HalQueueFormatter, HalTaskFormatter}
import org.huwtl.penfold.command._
import org.huwtl.penfold.domain.store.DomainRepository
import org.huwtl.penfold.app.support.json.{ObjectSerializer, EventSerializer}
import org.huwtl.penfold.readstore.{EventNotifiers, EventNotifier, NewEventsProvider}
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.FicusConfig._
import org.huwtl.penfold.app.support.{DateTimeSource, UUIDFactory}
import org.huwtl.penfold.app.schedule.{EventSyncScheduler, TaskArchiveScheduler, TaskTriggerScheduler}
import com.codahale.metrics.health.HealthCheckRegistry
import org.huwtl.penfold.app.support.metrics.{ReadStoreConnectivityHealthcheck, EventStoreConnectivityHealthcheck}
import org.huwtl.penfold.app.readstore.mongodb._
import org.huwtl.penfold.app.store.jdbc._
import com.mongodb.casbah.Imports._

class Bootstrap extends LifeCycle {
  override def init(context: ServletContext) {

    val config = ConfigFactory.load().as[ServerConfiguration]("penfold")

    val eventSerializer = new EventSerializer
    val objectSerializer = new ObjectSerializer

    val aggregateIdFactory = new UUIDFactory

    val domainJdbcPool = new JdbcDatabaseInitialiser().init(new JdbcConnectionPoolFactory().create(config.domainJdbcConnectionPool))
    val eventStore = new JdbcEventStore(domainJdbcPool, eventSerializer)
    val eventQueryService = new JdbcDomainEventQueryService(domainJdbcPool, eventSerializer)

    val readStoreServers = config.readStoreMongoDatabaseServers.servers.map(server => new ServerAddress(server.host, server.port))
    val readStoreDatabase = MongoConnection(readStoreServers)(config.readStoreMongoDatabaseServers.databaseName)
    val readStoreEventProvider = new NewEventsProvider(new MongoNextExpectedEventIdProvider("readStoreEventTracker", readStoreDatabase), eventQueryService)
    val readStoreUpdater = new EventNotifier(readStoreEventProvider, new MongoReadStoreUpdater(readStoreDatabase, new MongoEventTracker("readStoreEventTracker", readStoreDatabase), objectSerializer))

    val indexes = Indexes(config.readStoreIndexes)

    new IndexWriter().write(readStoreDatabase, indexes, config)

    val eventNotifiers = new EventNotifiers(List(readStoreUpdater))

    val domainRepository = new DomainRepository(eventStore, eventNotifiers)

    val commandDispatcher = new CommandDispatcherFactory(domainRepository, aggregateIdFactory).create

    val readStore = new MongoReadStore(readStoreDatabase, indexes, objectSerializer, new DateTimeSource)

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
    context mount(new TaskResource(readStore, commandDispatcher, objectSerializer, taskFormatter, config.pageSize, config.authentication), "/tasks/*")
    context mount(new QueueResource(readStore, commandDispatcher, objectSerializer, queueFormatter, config.pageSize, config.authentication), "/queues/*")

    new EventSyncScheduler(eventNotifiers, config.eventSync).start()

    new TaskTriggerScheduler(readStore, commandDispatcher, config.triggeredCheckFrequency).start()

    if (config.taskArchiver.isDefined) {
      new TaskArchiveScheduler(readStore, commandDispatcher, config.taskArchiver.get).start()
    }
  }
}
