package com.hlewis.rabbit_scheduler.queue

import com.rabbitmq.client.{Channel, ConnectionFactory}
import net.liftweb.amqp.AMQPDispatcher

class RabbitDispatcher[T](factory: ConnectionFactory, host: String, port: Int) extends AMQPDispatcher[T](factory, host, port) {
  override def configure(channel: Channel) {
    channel.exchangeDeclare("mult", "direct")
    channel.queueDeclare("mult_queue", true)
    channel.queueBind("mult_queue", "mult", "routeroute")
    channel.basicConsume("mult_queue", false, new RabbitConsumer(channel))
  }
}
