package org.huwtl.penfold.app

import javax.servlet.ServletContext
import org.scalatra.LifeCycle
import org.huwtl.penfold.app.web._
import org.huwtl.penfold.app.store.RedisEventStore
import java.net.URI
import org.huwtl.penfold.app.support.hal.{HalQueueFormatter, HalJobFormatter}
import java.util.concurrent.Executors._
import java.util.concurrent.TimeUnit._
import org.huwtl.penfold.command._
import org.huwtl.penfold.domain.store.DomainRepository
import com.redis.RedisClientPool
import org.huwtl.penfold.app.support.json.{JsonPathExtractor, ObjectSerializer, EventSerializer}
import org.huwtl.penfold.query._
import org.huwtl.penfold.app.query._
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

class Main extends LifeCycle {
  override def init(context: ServletContext) {

    val domainRedisClientPool = new RedisClientPool("localhost", 6379, database = 0)

    val queryRedisClientPool = new RedisClientPool("localhost", 6379, database = 1)

    val jsonExtractor = new JsonPathExtractor

    val redisKeyFactory = new RedisKeyFactory(jsonExtractor)

    val indexes = Indexes(List(
      Index("stuff", List(IndexField("stuff", "inner / stuff"))),
      Index("stuff2", List(IndexField("stuff", "inner / stuff"), IndexField("abc", "abc")))
    ), redisKeyFactory)

    val eventSerializer = new EventSerializer
    val objectSerializer = new ObjectSerializer

    val eventStore = new RedisEventStore(domainRedisClientPool, eventSerializer)

    val eventQueryService = new RedisEventStoreQueryService(domainRedisClientPool, eventSerializer)

    val queryStoreEventProvider = new NewEventsProvider(new RedisNextExpectedEventIdProvider(queryRedisClientPool, redisKeyFactory.eventTrackerKey("query")), eventQueryService)

    val queryStoreUpdater = new NewEventsNotifier(queryStoreEventProvider, new RedisQueryStoreUpdater(queryRedisClientPool, objectSerializer, redisKeyFactory))

    val indexUpdaters = indexes.all.map {
      index =>
        val searchEventProvider = new NewEventsProvider(new RedisNextExpectedEventIdProvider(queryRedisClientPool, redisKeyFactory.indexEventTrackerKey(index)), eventQueryService)
        new NewEventsNotifier(searchEventProvider, new RedisPayloadIndexUpdater(index, queryRedisClientPool, objectSerializer, redisKeyFactory))
    }

    val eventNotifiers = queryStoreUpdater :: indexUpdaters

    val domainRepository = new DomainRepository(eventStore, new NewEventsPublisher(eventNotifiers))

    val commandDispatcher = new CommandDispatcher(Map[Class[_ <: Command], CommandHandler[_ <: Command]](//
      classOf[CreateJob] -> new CreateJobHandler(domainRepository), //
      classOf[CreateFutureJob] -> new CreateFutureJobHandler(domainRepository), //
      classOf[TriggerJob] -> new TriggerJobHandler(domainRepository), //
      classOf[StartJob] -> new StartJobHandler(domainRepository), //
      classOf[CompleteJob] -> new CompleteJobHandler(domainRepository), //
      classOf[CancelJob] -> new CancelJobHandler(domainRepository) //
    ))

    val queryRepository = new RedisQueryRepository(queryRedisClientPool, indexes, objectSerializer, redisKeyFactory)

    val baseUrl = new URI("http://localhost:8080")

    val baseJobLink = new URI(s"${baseUrl.toString}/jobs")

    val baseQueueLink = new URI(s"${baseUrl.toString}/queues")

    val jobFormatter = new HalJobFormatter(baseJobLink, baseQueueLink)

    val queueFormatter = new HalQueueFormatter(baseQueueLink, jobFormatter)

    context mount(new JobResource(queryRepository, commandDispatcher, objectSerializer, jobFormatter), "/jobs/*")
    context mount(new QueueResource(queryRepository, commandDispatcher, objectSerializer, queueFormatter), "/queues/*")

    newSingleThreadScheduledExecutor.scheduleAtFixedRate(new Runnable() {
      def run() {
        try {
          queryRepository.retrieveWithPendingTrigger.foreach {
            jobRef => commandDispatcher.dispatch[TriggerJob](TriggerJob(jobRef.id))
          }
        }
        catch {
          case e: Exception => println(e)
        }
      }
    }, 0, 30, SECONDS)
  }
}
