package com.hlewis.rabbit_scheduler.di

import com.google.inject.servlet.{ServletModule, GuiceServletContextListener}
import com.google.inject.{Provides, Singleton, Guice, Injector}
import com.hlewis.rabbit_scheduler.api.JobstoreController
import com.hlewis.rabbit_scheduler.jobstore.{RedisJobstore, Jobstore}
import com.redis.RedisClient
import com.hlewis.rabbit_scheduler.queue.RabbitQueue
import com.rabbitmq.client.{ConnectionFactory, ConnectionParameters}

class Configuration extends GuiceServletContextListener {

  override def getInjector: Injector = {

    Guice.createInjector(new ServletModule() {
      override def configureServlets() {
        serve("/*").`with`(classOf[JobstoreController])
        bind(classOf[Jobstore]).to(classOf[RedisJobstore]).in(classOf[Singleton])
        bind(classOf[JobstoreController]).in(classOf[Singleton])
        bind(classOf[RabbitQueue]).in(classOf[Singleton])
      }

      @Provides
      def provideRedisClient(): RedisClient = {
        new RedisClient("localhost", 6379)
      }

      @Provides
      def providesRabbitConnectionFactory(): ConnectionFactory = {
        val params = new ConnectionParameters
        params.setUsername("qmg")
        params.setPassword("m4rl1n")
        params.setVirtualHost("qmg_vhost")

        new ConnectionFactory(params)
      }
    })

  }

}
