package com.hlewis.eventfire.app

import javax.servlet.ServletContext
import org.scalatra.LifeCycle
import com.hlewis.eventfire.usecases.{ExposePendingJobs, Refresh}
import akka.actor.ActorDSL._
import akka.actor.ActorSystem
import scala.concurrent.duration._
import com.hlewis.eventfire.app.feed.AtomServlet
import com.hlewis.eventfire.app.support.RepeatExecution
import com.hlewis.eventfire.app.store.redis.RedisJobStoreFactory
import com.hlewis.eventfire.app.web.AdminWebController

class Main extends LifeCycle with RedisJobStoreFactory {

  private implicit val system = ActorSystem("actor-system")

  override def init(context: ServletContext) {
    val jobStore = initJobStore()

    val exposePendingJobs = actor(new ExposePendingJobs())

    actor(new RepeatExecution(repeatDelay = 10 seconds, exposePendingJobs ! Refresh))

    context mount(classOf[AtomServlet], "/feed/*")

    context mount(new AdminWebController(jobStore), "/*")
  }

  override def destroy(context: ServletContext) {
    system.shutdown()
  }
}
