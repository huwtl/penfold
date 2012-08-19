package com.hlewis.rabbit_scheduler.queue

import com.rabbitmq.client.{Channel, ConnectionFactory, ConnectionParameters}
import net.liftweb.amqp._
import net.liftweb.actor.LiftActor
import com.google.inject.Inject

class RabbitQueue @Inject()(val connectionFactory: ConnectionFactory) {

  val amqp = new RabbitDispatcher[String](connectionFactory, "localhost", 5672)

  val sender = new StringAMQPSender(connectionFactory, "localhost", 5672, "mult", "routeroute")

  def send() {
    sender ! AMQPMessage("hello...")
  }

}
