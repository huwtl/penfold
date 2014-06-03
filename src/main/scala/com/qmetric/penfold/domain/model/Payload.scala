package com.qmetric.penfold.domain.model

object Payload {
  def empty = Payload(Map())
}

case class Payload(content: Map[String, Any])
