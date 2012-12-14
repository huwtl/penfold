package com.hlewis.rabbit_scheduler.app

import javax.servlet.ServletContext
import org.scalatra.LifeCycle

class Main extends LifeCycle with RabbitJobExchangeFactory with RedisJobStoreFactory {
  override def init(context: ServletContext) {
    var jobExchange = createJobExchange()

    var jobStore = createJobStore()

    context mount(new JobstoreController(jobStore, jobExchange), "/*")
  }
}
