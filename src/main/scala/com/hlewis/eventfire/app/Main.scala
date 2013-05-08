package com.hlewis.eventfire.app

import javax.servlet.ServletContext
import org.scalatra.LifeCycle
import com.hlewis.eventfire.usecases.RefreshFeedWithPendingJobs
import akka.actor.ActorDSL.actor
import akka.actor.ActorSystem
import scala.concurrent.duration._
import com.hlewis.eventfire.app.feed.AtomServlet
import com.hlewis.eventfire.app.support.PeriodicMessageSender
import com.hlewis.eventfire.app.store.redis.RedisJobStoreFactory
import com.hlewis.eventfire.app.web.AdminWebController

class Main extends LifeCycle with RedisJobStoreFactory {

  private implicit val system = ActorSystem("actor-system")

  override def init(context: ServletContext) {
    val pendingDispatchCheckQueue = actor(new PendingJobDispatchQueue(new RefreshFeedWithPendingJobs))

    actor(new PeriodicMessageSender(10 seconds, pendingDispatchCheckQueue, DispatchPending))

    val jobStore = initJobStore()

    context mount(classOf[AtomServlet], "/feed/*")

    context mount(new AdminWebController(jobStore), "/*")
  }

  override def destroy(context: ServletContext) {
    system.shutdown()
  }
}
