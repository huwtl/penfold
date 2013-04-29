package com.hlewis.rabbit_scheduler.app.exchange

import com.hlewis.rabbit_scheduler.domain.JobExchange
import akka.actor.ActorSystem
import com.github.sstone.amqp.{Amqp, RabbitMQConnection}
import com.github.sstone.amqp.Amqp.Publish
import com.hlewis.rabbit_scheduler.domain.Job

class RabbitJobExchange extends JobExchange {

  val consumer = new RabbitConsumer()

  def send() {
    implicit val system = ActorSystem("mySystem")

    // create an AMQP connection
    val conn = new RabbitMQConnection(host = "localhost", port = 5672, name = "Connection", user = "test", password = "password", vhost = "vhost")

    // create a "channel owner" on this connection
    val producer = conn.createChannelOwner()

    // wait till everyone is actually connected to the broker
    Amqp.waitForConnection(system, producer).await()

    // send a message
    producer ! Publish("mult", "routeroute", "hello...".getBytes, properties = None, mandatory = true, immediate = false)
  }

  def receive(job: Job) {}

  def dispatch(job: Job) {}
}
