package com.hlewis.rabbit_scheduler.app.exchange

import com.rabbitmq.client.ConnectionFactory

class RabbitDispatcher[T](factory: ConnectionFactory, host: String, port: Int) {
  //  override def configure(channel: Channel) {
  //    channel.exchangeDeclare("mult", "direct")
  //    channel.queueDeclare("mult_queue", true)
  //    channel.queueBind("mult_queue", "mult", "routeroute")
  //    channel.basicConsume("mult_queue", false, new RabbitConsumer(channel))
  //  }


}
