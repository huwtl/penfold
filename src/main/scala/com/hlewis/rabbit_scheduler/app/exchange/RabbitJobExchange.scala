package com.hlewis.rabbit_scheduler.app.exchange

import com.rabbitmq.client.ConnectionFactory
import net.liftweb.amqp._
import com.hlewis.rabbit_scheduler.domain.{Job, JobExchange}

class RabbitJobExchange(connectionFactory: ConnectionFactory) extends JobExchange {

  val amqp = new RabbitDispatcher[String](connectionFactory, "localhost", 5672)

  val sender = new StringAMQPSender(connectionFactory, "localhost", 5672, "mult", "routeroute")

  def send() {
    sender ! AMQPMessage("hello...")
  }

  def receive(job: Job) {}

  def dispatch(job: Job) {}
}
