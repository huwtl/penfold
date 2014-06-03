package com.qmetric.penfold.readstore

object EventSequenceId {
  val first = EventSequenceId(0)
}

case class EventSequenceId(value: Long)
