package org.huwtl.penfold.app

import javax.servlet.ServletContext
import org.scalatra.LifeCycle
import org.huwtl.penfold.app.web._
import java.net.URI
import org.huwtl.penfold.app.support.hal.{HalQueueFormatter, HalJobFormatter}
import org.huwtl.penfold.command._
import org.huwtl.penfold.domain.store.DomainRepository
import org.huwtl.penfold.app.support.json.{JsonPathExtractor, ObjectSerializer, EventSerializer}
import org.huwtl.penfold.query.{EventNotifiers, EventNotifier, NewEventsProvider}
import org.huwtl.penfold.app.query.redis._
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.FicusConfig._
import org.huwtl.penfold.command.CreateFutureJobHandler
import org.huwtl.penfold.command.StartJobHandler
import org.huwtl.penfold.command.CreateJobHandler
import org.huwtl.penfold.command.CancelJobHandler
import org.huwtl.penfold.command.CompleteJob
import org.huwtl.penfold.command.CompleteJobHandler
import org.huwtl.penfold.command.CreateFutureJob
import org.huwtl.penfold.command.StartJob
import org.huwtl.penfold.command.CreateJob
import org.huwtl.penfold.command.CancelJob
import org.huwtl.penfold.app.store.redis.RedisConnectionPoolFactory
import org.huwtl.penfold.app.support.UUIDFactory
import org.huwtl.penfold.app.schedule.JobTriggerScheduler
import org.huwtl.penfold.app.store.EventStoreConfigurationProvider

class Bootstrap extends LifeCycle {
  override def init(context: ServletContext) {

    val config = ConfigFactory.load().as[ServerConfiguration]("penfold")

    val redisKeyFactory = new RedisKeyFactory(new JsonPathExtractor)

    val eventSerializer = new EventSerializer
    val objectSerializer = new ObjectSerializer

    val aggregateIdFactory = new UUIDFactory

    val eventStoreConfig = new EventStoreConfigurationProvider(eventSerializer, config.domainConnectionPool).get()

    val queryStoreConnectionPool = new RedisConnectionPoolFactory().create(config.queryRedisConnectionPool)

    val queryStoreEventProvider = new NewEventsProvider(new RedisNextExpectedEventIdProvider(queryStoreConnectionPool, redisKeyFactory.eventTrackerKey("query")), eventStoreConfig.domainEventQueryService)

    val queryStoreUpdater = new EventNotifier(queryStoreEventProvider, new RedisQueryStoreUpdater(queryStoreConnectionPool, objectSerializer, redisKeyFactory))

    val indexes = Indexes(config.queryIndexes, redisKeyFactory)

    val indexUpdaters = indexes.all.map {
      index =>
        val searchEventProvider = new NewEventsProvider(new RedisNextExpectedEventIdProvider(queryStoreConnectionPool, redisKeyFactory.indexEventTrackerKey(index)), eventStoreConfig.domainEventQueryService)
        new EventNotifier(searchEventProvider, new RedisIndexUpdater(index, queryStoreConnectionPool, objectSerializer, redisKeyFactory))
    }

    val eventNotifiers = queryStoreUpdater :: indexUpdaters

    val domainRepository = new DomainRepository(eventStoreConfig.domainEventStore, new EventNotifiers(eventNotifiers))

    val commandDispatcher = new CommandDispatcher(Map[Class[_ <: Command], CommandHandler[_ <: Command]](//
      classOf[CreateJob] -> new CreateJobHandler(domainRepository, aggregateIdFactory), //
      classOf[CreateFutureJob] -> new CreateFutureJobHandler(domainRepository, aggregateIdFactory), //
      classOf[TriggerJob] -> new TriggerJobHandler(domainRepository), //
      classOf[StartJob] -> new StartJobHandler(domainRepository), //
      classOf[CompleteJob] -> new CompleteJobHandler(domainRepository), //
      classOf[CancelJob] -> new CancelJobHandler(domainRepository) //
    ))

    val queryRepository = new RedisQueryRepository(queryStoreConnectionPool, indexes, objectSerializer, redisKeyFactory)

    val baseUrl = URI.create(config.publicUrl)

    val baseJobLink = URI.create(s"${baseUrl.toString}/jobs")

    val baseQueueLink = URI.create(s"${baseUrl.toString}/queues")

    val jobFormatter = new HalJobFormatter(baseJobLink, baseQueueLink)

    val queueFormatter = new HalQueueFormatter(baseQueueLink, jobFormatter)

    context mount(new PingResource, "/ping")
    context mount(new JobResource(queryRepository, commandDispatcher, objectSerializer, jobFormatter, config.authentication), "/jobs/*")
    context mount(new QueueResource(queryRepository, commandDispatcher, objectSerializer, queueFormatter, config.authentication), "/queues/*")

    new JobTriggerScheduler(queryRepository, commandDispatcher, config.triggeredCheckFrequency).start()
  }
}
