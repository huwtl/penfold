package org.huwtl.penfold.app

import javax.servlet.ServletContext
import org.scalatra.LifeCycle
import org.huwtl.penfold.app.web._
import org.huwtl.penfold.app.store.RedisEventStore
import java.net.URI
import org.huwtl.penfold.app.support.hal.{HalTriggeredJobFeedFormatter, HalStartedJobFormatter, HalJobFormatter, HalCompletedJobFormatter}
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

    val jobLink = new URI(s"${baseUrl.toString}/jobs")

    val triggeredJobLink = new URI(s"${baseUrl.toString}/feed/triggered")

    val startedJobLink = new URI(s"${baseUrl.toString}/feed/started")

    val completedJobLink = new URI(s"${baseUrl.toString}/feed/completed")

    val jobFormatter = new HalJobFormatter(jobLink, triggeredJobLink)

    val triggeredJobFeedFormatter = new HalTriggeredJobFeedFormatter(triggeredJobLink, jobLink, startedJobLink)

    val startedJobFormatter = new HalStartedJobFormatter(startedJobLink, jobLink, completedJobLink)

    val completedJobFormatter = new HalCompletedJobFormatter(completedJobLink, jobLink)

    context mount(new JobsResource(queryRepository, commandDispatcher, objectSerializer, jobFormatter), "/jobs/*")
    context mount(new TriggeredJobFeedResource(queryRepository, triggeredJobFeedFormatter), "/feed/triggered/*")
    context mount(new StartedJobFeedResource(commandDispatcher, queryRepository, objectSerializer, startedJobFormatter), "/feed/started/*")
    context mount(new CompletedJobFeedResource(queryRepository, commandDispatcher, objectSerializer, completedJobFormatter), "/feed/completed/*")

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
