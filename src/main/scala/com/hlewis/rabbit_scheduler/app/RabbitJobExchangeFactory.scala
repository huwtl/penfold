package com.hlewis.rabbit_scheduler.app

import exchange.RabbitJobExchange

trait RabbitJobExchangeFactory {
  def createJobExchange(): RabbitJobExchange = {
//    val params = new ConnectionParameters
//    params.setUsername("qmg")
//    params.setPassword("m4rl1n")
//    params.setVirtualHost("qmg_vhost")

//    val connectionFactory = new ConnectionFactory(params)

    new RabbitJobExchange()
  }
}
