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
import org.huwtl.penfold.command.CreateJob
import org.huwtl.penfold.domain.store.DomainRepository
import com.redis.RedisClient
import org.huwtl.penfold.app.support.json.{ObjectSerializer, EventSerializer}
import org.huwtl.penfold.query.{RedisQueryRepository, RedisQueryStoreEventPersister, RedisNewEventsProvider, QueryStoreUpdater}

class Main extends LifeCycle {
  override def init(context: ServletContext) {

    val redisClient = new RedisClient("localhost", 6379)
    val eventSerializer = new EventSerializer
    val objectSerializer = new ObjectSerializer
    val eventStore = new RedisEventStore(redisClient, eventSerializer)

    val domainRepository = new DomainRepository(eventStore, new QueryStoreUpdater(new RedisNewEventsProvider(redisClient, eventSerializer), new RedisQueryStoreEventPersister(redisClient, objectSerializer)))

    val commandDispatcher = new CommandDispatcher(Map[Class[_ <: Command], CommandHandler[_ <: Command]](//
      classOf[CreateJob] -> new CreateJobHandler(domainRepository), //
      classOf[CreateFutureJob] -> new CreateFutureJobHandler(domainRepository), //
      classOf[TriggerJob] -> new TriggerJobHandler(domainRepository), //
      classOf[StartJob] -> new StartJobHandler(domainRepository), //
      classOf[CompleteJob] -> new CompleteJobHandler(domainRepository), //
      classOf[CancelJob] -> new CancelJobHandler(domainRepository) //
    ))

    val queryRepository = new RedisQueryRepository(redisClient, objectSerializer)

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
