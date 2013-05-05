package com.hlewis.eventfire.app

import exchange.RabbitJobExchange

trait RabbitJobExchangeFactory {
  def createJobExchange(): RabbitJobExchange = {
//    val params = new ConnectionParameters
//    params.setUsername("test")
//    params.setPassword("password")
//    params.setVirtualHost("vhost")

//    val connectionFactory = new ConnectionFactory(params)

    new RabbitJobExchange()
  }
}
