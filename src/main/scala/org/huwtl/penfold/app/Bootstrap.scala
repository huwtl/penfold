package org.huwtl.penfold.app

import javax.servlet.ServletContext
import org.scalatra.LifeCycle
import org.huwtl.penfold.app.web._
import java.net.URI
import org.huwtl.penfold.app.support.hal.{HalQueueFormatter, HalJobFormatter}
import java.util.concurrent.Executors._
import org.huwtl.penfold.command._
import org.huwtl.penfold.domain.store.{EventStore, DomainRepository}
import com.redis.RedisClientPool
import org.huwtl.penfold.app.support.json.{JsonPathExtractor, ObjectSerializer, EventSerializer}
import org.huwtl.penfold.query.{EventStoreQueryService, NewEventsPublisher, NewEventsNotifier, NewEventsProvider}
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
import org.slf4j.LoggerFactory
import org.huwtl.penfold.app.store.redis.{RedisEventStoreQueryService, RedisEventStore}
import org.huwtl.penfold.app.support.UUIDFactory
import com.mchange.v2.c3p0.ComboPooledDataSource
import com.googlecode.flyway.core.Flyway
import scala.slick.driver.JdbcDriver.backend.Database
import org.huwtl.penfold.app.store.jdbc.{JdbcEventStoreQueryService, JdbcEventStore}

class Bootstrap extends LifeCycle {
  private val logger = LoggerFactory.getLogger(getClass)

  override def init(context: ServletContext) {

    val config = ConfigFactory.load().as[ServerConfiguration]("penfold")

    val queryRedisClientPool = new RedisClientPool(
      config.queryRedisConnectionPool.host,
      config.queryRedisConnectionPool.port,
      config.queryRedisConnectionPool.poolSize,
      config.queryRedisConnectionPool.database,
      config.queryRedisConnectionPool.password)

    val redisKeyFactory = new RedisKeyFactory(new JsonPathExtractor)

    val indexes = Indexes(config.queryIndexes, redisKeyFactory)

    val eventSerializer = new EventSerializer
    val objectSerializer = new ObjectSerializer

    val aggregateIdFactory = new UUIDFactory

    val domainStoreConfig: (EventStore, EventStoreQueryService) = config.domainConnectionPool match {
      case Left(jdbcPool) => {
        val domainJdbcPool = configureJdbcConnectionPool(jdbcPool)
        val eventStore = new JdbcEventStore(domainJdbcPool, eventSerializer)
        val eventQueryService = new JdbcEventStoreQueryService(domainJdbcPool, eventSerializer)
        (eventStore, eventQueryService)
      }
      case Right(redisPool) => {
        val domainRedisPool = configureRedisConnectionPool(redisPool)
        val eventStore = new RedisEventStore(domainRedisPool, eventSerializer)
        val eventQueryService = new RedisEventStoreQueryService(domainRedisPool, eventSerializer)
        (eventStore, eventQueryService)
      }
    }

    val eventStore = domainStoreConfig._1

    val eventQueryService = domainStoreConfig._2

    val queryStoreEventProvider = new NewEventsProvider(new RedisNextExpectedEventIdProvider(queryRedisClientPool, redisKeyFactory.eventTrackerKey("query")), eventQueryService)

    val queryStoreUpdater = new NewEventsNotifier(queryStoreEventProvider, new RedisQueryStoreUpdater(queryRedisClientPool, objectSerializer, redisKeyFactory))

    val indexUpdaters = indexes.all.map {
      index =>
        val searchEventProvider = new NewEventsProvider(new RedisNextExpectedEventIdProvider(queryRedisClientPool, redisKeyFactory.indexEventTrackerKey(index)), eventQueryService)
        new NewEventsNotifier(searchEventProvider, new RedisIndexUpdater(index, queryRedisClientPool, objectSerializer, redisKeyFactory))
    }

    val eventNotifiers = queryStoreUpdater :: indexUpdaters

    val domainRepository = new DomainRepository(eventStore, new NewEventsPublisher(eventNotifiers))

    val commandDispatcher = new CommandDispatcher(Map[Class[_ <: Command], CommandHandler[_ <: Command]](//
      classOf[CreateJob] -> new CreateJobHandler(domainRepository, aggregateIdFactory), //
      classOf[CreateFutureJob] -> new CreateFutureJobHandler(domainRepository, aggregateIdFactory), //
      classOf[TriggerJob] -> new TriggerJobHandler(domainRepository), //
      classOf[StartJob] -> new StartJobHandler(domainRepository), //
      classOf[CompleteJob] -> new CompleteJobHandler(domainRepository), //
      classOf[CancelJob] -> new CancelJobHandler(domainRepository) //
    ))

    val queryRepository = new RedisQueryRepository(queryRedisClientPool, indexes, objectSerializer, redisKeyFactory)

    val baseUrl = URI.create(config.publicUrl)

    val baseJobLink = URI.create(s"${baseUrl.toString}/jobs")

    val baseQueueLink = URI.create(s"${baseUrl.toString}/queues")

    val jobFormatter = new HalJobFormatter(baseJobLink, baseQueueLink)

    val queueFormatter = new HalQueueFormatter(baseQueueLink, jobFormatter)

    context mount(new PingResource, "/ping")
    context mount(new JobResource(queryRepository, commandDispatcher, objectSerializer, jobFormatter), "/jobs/*")
    context mount(new QueueResource(queryRepository, commandDispatcher, objectSerializer, queueFormatter), "/queues/*")

    newSingleThreadScheduledExecutor.scheduleAtFixedRate(new Runnable() {
      def run() {
        try {
          queryRepository.retrieveJobsToQueue.foreach {
            jobRef => commandDispatcher.dispatch[TriggerJob](TriggerJob(jobRef.id))
          }
        }
        catch {
          case e: Exception => logger.error("error checking for jobs to check", e)
        }
      }
    }, 0, config.triggeredCheckFrequency.length, config.triggeredCheckFrequency.unit)
  }

  private def configureRedisConnectionPool(poolConfig: RedisConnectionPool) = {
    new RedisClientPool(
      poolConfig.host,
      poolConfig.port,
      poolConfig.poolSize,
      poolConfig.database,
      poolConfig.password)
  }

  private def configureJdbcConnectionPool(poolConfig: JdbcConnectionPool) = {
    val dataSource = new ComboPooledDataSource

    dataSource.setDriverClass(poolConfig.driver)
    dataSource.setJdbcUrl(poolConfig.url)
    dataSource.setUser(poolConfig.username)
    dataSource.setPassword(poolConfig.password)
    dataSource.setMaxPoolSize(poolConfig.poolSize)
    dataSource.setPreferredTestQuery("select 1")
    dataSource.setIdleConnectionTestPeriod(60)

    val flyway = new Flyway
    flyway.setDataSource(dataSource)
    flyway.migrate()

    Database.forDataSource(dataSource)
  }
}
