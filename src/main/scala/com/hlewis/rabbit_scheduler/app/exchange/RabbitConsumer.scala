package com.hlewis.rabbit_scheduler.app.exchange

import com.github.sstone.amqp.Amqp.{ExchangeParameters, QueueParameters, Delivery, Ack}
import akka.actor.{ActorSystem, Props, Actor}
import com.github.sstone.amqp.{Amqp, RabbitMQConnection}

class RabbitConsumer {
  implicit val system = ActorSystem("mySystem")

  // create an AMQP connection
  val conn = new RabbitMQConnection(host = "localhost", port = 5672, name = "Connection", user = "test", password = "password", vhost = "vhost")

  // create an actor that will receive AMQP deliveries
  val listener = system.actorOf(Props(new Actor {
    def receive = {
      case Delivery(consumerTag, envelope, properties, body) => {
        println("got a message: " + new String(body))
        sender ! Ack(envelope.getDeliveryTag)
      }
    }
  }))

  // create a consumer that will route incoming AMQP messages to our listener
  val queueParams = QueueParameters("mult_queue", passive = true, durable = true, exclusive = true, autodelete = false)
  val consumer = conn.createConsumer(ExchangeParameters("mult", durable = true, autodelete = false, passive = true, exchangeType = "direct"), queueParams, "routeroute", listener, None)
  // wait till everyone is actually connected to the broker
  Amqp.waitForConnection(system, consumer).await()
}
