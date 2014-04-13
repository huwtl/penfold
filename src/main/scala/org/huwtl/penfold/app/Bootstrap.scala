package org.huwtl.penfold.app

import javax.servlet.ServletContext
import org.scalatra.LifeCycle
import org.huwtl.penfold.app.web._
import java.net.URI
import org.huwtl.penfold.app.support.hal.{HalQueueFormatter, HalJobFormatter}
import org.huwtl.penfold.command._
import org.huwtl.penfold.domain.store.DomainRepository
import org.huwtl.penfold.app.support.json.{ObjectSerializer, EventSerializer}
import org.huwtl.penfold.readstore.{EventNotifiers, EventNotifier, NewEventsProvider}
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.FicusConfig._
import org.huwtl.penfold.app.support.{DateTimeSource, UUIDFactory}
import org.huwtl.penfold.app.schedule.JobTriggerScheduler
import com.codahale.metrics.health.HealthCheckRegistry
import org.huwtl.penfold.app.support.metrics.{ReadStoreConnectivityHealthcheck, EventStoreConnectivityHealthcheck}
import org.huwtl.penfold.app.readstore.mongodb._
import org.huwtl.penfold.app.store.jdbc._
import org.huwtl.penfold.command.CreateFutureJobHandler
import org.huwtl.penfold.command.StartJobHandler
import org.huwtl.penfold.command.CreateJobHandler
import org.huwtl.penfold.command.CancelJobHandler
import org.huwtl.penfold.command.CompleteJob
import org.huwtl.penfold.command.TriggerJob
import org.huwtl.penfold.command.TriggerJobHandler
import org.huwtl.penfold.command.CompleteJobHandler
import org.huwtl.penfold.command.CreateFutureJob
import org.huwtl.penfold.command.StartJob
import org.huwtl.penfold.command.CreateJob
import org.huwtl.penfold.command.CancelJob
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
    indexes.all.map(index =>
      readStoreDatabase("jobs").ensureIndex(MongoDBObject(index.fields.map(f => f.key -> 1)), MongoDBObject("background" -> true))
    )

    val eventNotifiers = List(readStoreUpdater)

    val domainRepository = new DomainRepository(eventStore, new EventNotifiers(eventNotifiers))

    val commandDispatcher = new CommandDispatcher(Map[Class[_ <: Command], CommandHandler[_ <: Command]](//
      classOf[CreateJob] -> new CreateJobHandler(domainRepository, aggregateIdFactory), //
      classOf[CreateFutureJob] -> new CreateFutureJobHandler(domainRepository, aggregateIdFactory), //
      classOf[TriggerJob] -> new TriggerJobHandler(domainRepository), //
      classOf[StartJob] -> new StartJobHandler(domainRepository), //
      classOf[CompleteJob] -> new CompleteJobHandler(domainRepository), //
      classOf[CancelJob] -> new CancelJobHandler(domainRepository) //
    ))

    val readStore = new MongoReadStore(readStoreDatabase, objectSerializer, new DateTimeSource)

    val baseUrl = URI.create(config.publicUrl)

    val baseJobLink = URI.create(s"${baseUrl.toString}/jobs")

    val baseQueueLink = URI.create(s"${baseUrl.toString}/queues")

    val jobFormatter = new HalJobFormatter(baseJobLink, baseQueueLink)

    val queueFormatter = new HalQueueFormatter(baseQueueLink, jobFormatter)

    val healthCheckRegistry: HealthCheckRegistry = new HealthCheckRegistry
    healthCheckRegistry.register("Event store connectivity", new EventStoreConnectivityHealthcheck(eventStore))
    healthCheckRegistry.register("Read store connectivity", new ReadStoreConnectivityHealthcheck(readStore))

    context mount(new PingResource, "/ping")
    context mount(new HealthCheckResource(healthCheckRegistry, objectSerializer), "/healthcheck")
    context mount(new JobResource(readStore, commandDispatcher, objectSerializer, jobFormatter, config.pageSize, config.authentication), "/jobs/*")
    context mount(new QueueResource(readStore, commandDispatcher, objectSerializer, queueFormatter, config.pageSize, config.authentication), "/queues/*")

    new JobTriggerScheduler(readStore, commandDispatcher, config.triggeredCheckFrequency).start()
  }
}
