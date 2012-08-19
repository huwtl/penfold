package com.hlewis.rabbit_scheduler.queue

import com.rabbitmq.client.{AMQP, Envelope, DefaultConsumer, Channel}
import java.io.{ByteArrayInputStream, ObjectInputStream}

class RabbitConsumer[T](channel: Channel) extends DefaultConsumer(channel) {
  override def handleDelivery(tag: String, env: Envelope, props: AMQP.BasicProperties, body: Array[Byte]) {
    val deliveryTag = env.getDeliveryTag
    val in = new ObjectInputStream(new ByteArrayInputStream(body))
    val t = in.readObject.asInstanceOf[T]

    println("got: " + t)

    channel.basicAck(deliveryTag, false)
  }
}
